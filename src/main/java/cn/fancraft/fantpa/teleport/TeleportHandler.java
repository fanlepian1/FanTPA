package cn.fancraft.fantpa.teleport;

import cn.fancraft.fantpa.api.PlayerManager;
import cn.fancraft.fantpa.api.TPManager;
import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.event.EventManager;
import cn.fancraft.fantpa.event.TeleportEvents;
import cn.fancraft.fantpa.message.MessageManager;
import cn.fancraft.fantpa.utils.LoggerUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class TeleportHandler {

    private static final TeleportHandler INSTANCE = new TeleportHandler();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DATA_DIR = Paths.get("config", "fantpa", "data");
    private static final Path HOMES_FILE = DATA_DIR.resolve("homes.json");
    private static final Type HOMES_TYPE = new TypeToken<Map<String, Map<String, HomeData>>>() {}.getType();

    private final Map<UUID, TeleportRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, HomeData>> homes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private MinecraftServer server;

    public Map<UUID, TeleportRequest> pendingRequests() { return Collections.unmodifiableMap(pendingRequests); }

    public static TeleportHandler getInstance() { return INSTANCE; }

    public void init(MinecraftServer server) {
        this.server = server;
        loadHomes();
        startTimeoutChecker();
    }

    public void shutdown() {
        saveHomes();
        scheduler.shutdown();
    }

    // ==================== TPA ====================

    public boolean requestTpa(ServerPlayer sender, ServerPlayer target) {
        if (!checkCooldown(sender)) return false;

        if (getPendingRequestFrom(sender, target) != null) {
            MessageManager.sendError(sender, "tpa.already_sent");
            return false;
        }

        TeleportRequest request = new TeleportRequest(sender, target, TeleportRequest.Type.TPA);
        pendingRequests.put(request.getId(), request);
        EventManager.getInstance().fire(new TeleportEvents.TpaRequestEvent(sender, target));

        String targetName = target.getDisplayName().getString();
        String senderName = sender.getDisplayName().getString();

        MessageManager.send(sender, "tpa.sent", Map.of("player", targetName));

        String acceptCmd = "/tpaccept " + senderName;
        String denyCmd = "/tpdeny " + senderName;

        MessageManager.send(target, "tpa.header");
        MessageManager.send(target, "tpa.received", Map.of("player", senderName));
        MessageManager.sendAcceptDenyButtons(target, acceptCmd, denyCmd);
        MessageManager.send(target, "tpa.footer");

        return true;
    }

    public boolean requestTpaHere(ServerPlayer sender, ServerPlayer target) {
        if (!checkCooldown(sender)) return false;

        if (getPendingRequestFrom(sender, target) != null) {
            MessageManager.sendError(sender, "tpahere.already_sent");
            return false;
        }

        TeleportRequest request = new TeleportRequest(sender, target, TeleportRequest.Type.TPAHERE);
        pendingRequests.put(request.getId(), request);
        EventManager.getInstance().fire(new TeleportEvents.TpaHereRequestEvent(sender, target));

        String targetName = target.getDisplayName().getString();
        String senderName = sender.getDisplayName().getString();

        MessageManager.send(sender, "tpahere.sent", Map.of("player", targetName));

        String acceptCmd = "/tpaccept " + senderName;
        String denyCmd = "/tpdeny " + senderName;

        MessageManager.send(target, "tpahere.header");
        MessageManager.send(target, "tpahere.received", Map.of("player", senderName));
        MessageManager.sendAcceptDenyButtons(target, acceptCmd, denyCmd);
        MessageManager.send(target, "tpahere.footer");

        return true;
    }

    public boolean acceptRequest(ServerPlayer target, String senderName) {
        ServerPlayer sender = PlayerManager.getPlayer(senderName);
        if (sender == null) {
            MessageManager.sendError(target, "tpa.player_offline");
            return false;
        }

        TeleportRequest request = getPendingRequestFrom(sender, target);
        if (request == null) {
            MessageManager.sendError(target, "tpa.no_request");
            return false;
        }

        request.setState(TeleportRequest.State.ACCEPTED);
        pendingRequests.remove(request.getId());
        EventManager.getInstance().fire(new TeleportEvents.TpaAcceptEvent(sender, target));

        ServerPlayer teleportPlayer = request.getTeleportPlayer();
        ServerPlayer destPlayer = request.getDestinationPlayer();

        int delay = ConfigManager.getInstance().getConfig().teleportDelay;
        String type = request.getType() == TeleportRequest.Type.TPA ? "tpa" : "tpahere";

        MessageManager.sendSuccess(teleportPlayer, "tpa.accepted_delay",
            Map.of("player", destPlayer.getDisplayName().getString(), "seconds", String.valueOf(delay)));
        MessageManager.sendSuccess(destPlayer, "tpa.accepted_delay",
            Map.of("player", teleportPlayer.getDisplayName().getString(), "seconds", String.valueOf(delay)));

        scheduler.schedule(() -> {
            if (!PlayerManager.isPlayerOnline(teleportPlayer.getDisplayName().getString()) ||
                !PlayerManager.isPlayerOnline(destPlayer.getDisplayName().getString())) {
                MessageManager.sendError(teleportPlayer, "tpa.player_offline");
                MessageManager.sendError(destPlayer, "tpa.player_offline");
                return;
            }

            boolean success = TPManager.teleportToPlayer(teleportPlayer, destPlayer);
            if (success) {
                request.setState(TeleportRequest.State.COMPLETED);
                applyCooldown(teleportPlayer);

                MessageManager.sendSuccess(teleportPlayer, "tpa.teleporting",
                    Map.of("player", destPlayer.getDisplayName().getString()));
                MessageManager.sendSuccess(destPlayer, "tpa.accepted",
                    Map.of("player", teleportPlayer.getDisplayName().getString()));

                EventManager.getInstance().fireAsync(
                    new TeleportEvents.TeleportEvent(teleportPlayer, destPlayer, type));
            } else {
                MessageManager.sendError(teleportPlayer, "tpa.failed");
                MessageManager.sendError(destPlayer, "tpa.failed");
            }
        }, delay, TimeUnit.SECONDS);

        return true;
    }

    public boolean denyRequest(ServerPlayer target, String senderName) {
        ServerPlayer sender = PlayerManager.getPlayer(senderName);
        if (sender == null) {
            MessageManager.sendError(target, "tpa.player_offline");
            return false;
        }

        TeleportRequest request = getPendingRequestFrom(sender, target);
        if (request == null) {
            MessageManager.sendError(target, "tpa.no_request");
            return false;
        }

        request.setState(TeleportRequest.State.DENIED);
        pendingRequests.remove(request.getId());
        EventManager.getInstance().fire(new TeleportEvents.TpaDenyEvent(sender, target));

        String targetName = target.getDisplayName().getString();
        String senderNameDisplay = sender.getDisplayName().getString();

        MessageManager.send(sender, "tpa.denied_by_player", Map.of("player", targetName));
        MessageManager.sendSuccess(target, "tpa.denied", Map.of("player", senderNameDisplay));

        return true;
    }

    // ==================== Back ====================

    public boolean back(ServerPlayer player) {
        if (!checkCooldown(player)) return false;

        GlobalPos deathPos = PlayerManager.getLastDeathPosition(player);
        if (deathPos != null) {
            MessageManager.send(player, "back.header");
            boolean success = TPManager.teleportToDeathPoint(player, deathPos);
            if (success) {
                applyCooldown(player);
                MessageManager.sendSuccess(player, "back.success");
                MessageManager.send(player, "back.footer");
                EventManager.getInstance().fireAsync(new TeleportEvents.BackEvent(player, "death"));
                return true;
            }
        }

        MessageManager.sendError(player, "back.no_location");
        return false;
    }

    // ==================== Home ====================

    public boolean setHome(ServerPlayer player, String homeName) {
        Map<String, HomeData> playerHomes = homes.computeIfAbsent(
            player.getUUID(), k -> new LinkedHashMap<>());
        String name = (homeName != null) ? homeName : "home";

        MessageManager.send(player, "home.header");
        playerHomes.put(name, new HomeData(player));
        saveHomes();
        EventManager.getInstance().fireAsync(new TeleportEvents.HomeSetEvent(player, name));
        MessageManager.sendSuccess(player, "home.set", Map.of("home", name));
        MessageManager.send(player, "home.footer");
        return true;
    }

    public boolean deleteHome(ServerPlayer player, String homeName) {
        Map<String, HomeData> playerHomes = homes.get(player.getUUID());
        String name = (homeName != null) ? homeName : "home";

        MessageManager.send(player, "home.header");
        if (playerHomes == null || !playerHomes.containsKey(name)) {
            MessageManager.sendError(player, "home.not_found", Map.of("home", name));
            return false;
        }

        playerHomes.remove(name);
        saveHomes();
        MessageManager.sendSuccess(player, "home.deleted", Map.of("home", name));
        MessageManager.send(player, "home.footer");
        return true;
    }

    public boolean home(ServerPlayer player, String homeName) {
        if (!checkCooldown(player)) return false;

        Map<String, HomeData> playerHomes = homes.get(player.getUUID());
        String name = (homeName != null) ? homeName : "home";

        if (playerHomes == null || playerHomes.isEmpty())
            return setHome(player, name);

        HomeData home = playerHomes.get(name);
        if (home == null) {
            MessageManager.sendError(player, "home.not_found", Map.of("home", name));
            return false;
        }

        GlobalPos pos = home.toGlobalPos(server);
        if (pos == null) {
            MessageManager.sendError(player, "home.failed");
            return false;
        }

        MessageManager.send(player, "home.header");
        boolean success = TPManager.teleportToPosition(player, pos);
        if (success) {
            applyCooldown(player);
            MessageManager.sendSuccess(player, "home.teleported", Map.of("home", name));
            MessageManager.send(player, "home.footer");
            EventManager.getInstance().fireAsync(new TeleportEvents.HomeTeleportEvent(player, name));
        } else {
            MessageManager.sendError(player, "home.failed");
        }
        return success;
    }

    // ==================== 管理员 ====================

    public boolean tpAll(ServerPlayer admin) {
        MessageManager.send(admin, "tpall.header");
        int count = 0;
        String adminName = admin.getDisplayName().getString();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == admin) continue;
            TPManager.teleportToPlayer(player, admin);
            count++;
        }

        MessageManager.sendSuccess(admin, "tpall.success", Map.of("count", String.valueOf(count)));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == admin) continue;
            MessageManager.send(player, "tpall.notify", Map.of("player", adminName));
        }
        MessageManager.send(admin, "tpall.footer");
        return true;
    }

    // ==================== 辅助 ====================

    private boolean checkCooldown(ServerPlayer player) {
        Long lastTime = cooldowns.get(player.getUUID());
        if (lastTime != null) {
            int cooldown = ConfigManager.getInstance().getConfig().teleportCooldown;
            long elapsed = System.currentTimeMillis() - lastTime;
            if (elapsed < cooldown * 1000L) {
                long remaining = cooldown - elapsed / 1000;
                MessageManager.sendError(player, "cooldown.active",
                    Map.of("seconds", String.valueOf(remaining)));
                return false;
            }
        }
        return true;
    }

    private void applyCooldown(ServerPlayer player) {
        cooldowns.put(player.getUUID(), System.currentTimeMillis());
    }

    private TeleportRequest getPendingRequestFrom(ServerPlayer sender, ServerPlayer target) {
        for (TeleportRequest r : pendingRequests.values()) {
            if (r.getSender().getUUID().equals(sender.getUUID()) &&
                r.getTarget().getUUID().equals(target.getUUID()) &&
                r.getState() == TeleportRequest.State.PENDING)
                return r;
        }
        return null;
    }

    private void startTimeoutChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            int timeout = ConfigManager.getInstance().getConfig().teleportTimeout;
            var it = pendingRequests.values().iterator();
            while (it.hasNext()) {
                TeleportRequest r = it.next();
                if (r.isExpired(timeout)) {
                    r.setState(TeleportRequest.State.TIMEOUT);
                    it.remove();

                    String targetName = r.getTarget().getDisplayName().getString();
                    String senderName = r.getSender().getDisplayName().getString();

                    MessageManager.sendError(r.getSender(), "tpa.timeout",
                        Map.of("player", targetName));
                    MessageManager.sendError(r.getTarget(), "tpa.timeout_target",
                        Map.of("player", senderName));
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    // ==================== Homes 持久化 ====================

    private void loadHomes() {
        try {
            Files.createDirectories(DATA_DIR);
            if (Files.exists(HOMES_FILE)) {
                String content = Files.readString(HOMES_FILE);
                Map<String, Map<String, HomeData>> loaded = GSON.fromJson(content, HOMES_TYPE);
                if (loaded != null) {
                    for (var entry : loaded.entrySet()) {
                        try { homes.put(UUID.fromString(entry.getKey()), entry.getValue()); }
                        catch (IllegalArgumentException ignored) {}
                    }
                }
                LoggerUtil.info("家园数据已加载: " + homes.size() + " 个玩家");
            }
        } catch (IOException e) { LoggerUtil.error("家园数据加载失败", e); }
    }

    public void saveHomes() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.writeString(HOMES_FILE, GSON.toJson(homes));
        } catch (IOException e) { LoggerUtil.error("家园数据保存失败", e); }
    }

    // ==================== HomeData ====================

    public static class HomeData {
        private String dimension;
        private int x, y, z;

        public HomeData() {}

        public HomeData(ServerPlayer player) {
            this.dimension = ((ServerLevel) player.level()).dimension().toString();
            BlockPos pos = player.blockPosition();
            this.x = pos.getX(); this.y = pos.getY(); this.z = pos.getZ();
        }

        public GlobalPos toGlobalPos(MinecraftServer server) {
            for (ServerLevel level : server.getAllLevels()) {
                if (level.dimension().toString().equals(dimension))
                    return GlobalPos.of(level.dimension(), new BlockPos(x, y, z));
            }
            return null;
        }
    }
}
