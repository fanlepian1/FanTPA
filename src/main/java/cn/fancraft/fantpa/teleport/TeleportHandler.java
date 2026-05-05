package cn.fancraft.fantpa.teleport;

import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.event.EventManager;
import cn.fancraft.fantpa.event.TeleportEvents;
import cn.fancraft.fantpa.message.MessageManager;
import cn.fancraft.fantpa.utils.PlayerDataManager;
import cn.fancraft.fantpa.utils.PlayerManager;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.*;
import java.util.concurrent.*;

public class TeleportHandler {

    private static final TeleportHandler INSTANCE = new TeleportHandler();

    private final Map<UUID, TeleportRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private MinecraftServer server;

    public Map<UUID, TeleportRequest> pendingRequests() { return Collections.unmodifiableMap(pendingRequests); }
    public static TeleportHandler getInstance() { return INSTANCE; }

    public void init(MinecraftServer server) { this.server = server; startTimeoutChecker(); }
    public void shutdown() { scheduler.shutdown(); }

    // ==================== Teleport ====================

    private boolean doTeleport(ServerPlayer player, ServerPlayer target) {
        return player.teleportTo((ServerLevel) target.level(),
            target.getX(), target.getY(), target.getZ(), Set.of(),
            target.getYRot(), target.getXRot(), true);
    }

    private boolean doTeleport(ServerPlayer player, ServerLevel level, double x, double y, double z) {
        return player.teleportTo(level, x, y, z, Set.of(),
            player.getYRot(), player.getXRot(), true);
    }

    /**
     * Async chunk loading then teleport on main thread.
     */
    private void teleportAsync(ServerPlayer player, ServerLevel level, double x, double y, double z,
                               Runnable onSuccess, Runnable onFail) {
        int chunkX = SectionPos.blockToSectionCoord(x);
        int chunkZ = SectionPos.blockToSectionCoord(z);

        level.getChunkSource().getChunkFuture(chunkX, chunkZ, ChunkStatus.FULL, true)
            .thenAccept(chunkResult -> {
                if (chunkResult.isSuccess()) {
                    server.execute(() -> {
                        if (doTeleport(player, level, x, y, z)) {
                            onSuccess.run();
                        } else {
                            onFail.run();
                        }
                    });
                } else {
                    server.execute(onFail);
                }
            });
    }

    // ==================== TPA ====================

    public boolean requestTeleport(ServerPlayer sender, ServerPlayer target, TeleportRequest.Type type) {
        if (!checkCooldown(sender)) return false;

        if (getPendingRequestFrom(sender, target) != null) {
            MessageManager.sendError(sender, type == TeleportRequest.Type.TPA ? "tpa.already_sent" : "tpahere.already_sent");
            return false;
        }

        TeleportRequest r = new TeleportRequest(sender, target, type);
        pendingRequests.put(r.getId(), r);
        EventManager.getInstance().fire(new TeleportEvents.TpaEvent(sender, target,
            type == TeleportRequest.Type.TPA ? TeleportEvents.TpaEvent.Type.REQUEST : TeleportEvents.TpaEvent.Type.HERE_REQUEST));

        String senderName = sender.getDisplayName().getString();
        String targetName = target.getDisplayName().getString();
        String prefix = type == TeleportRequest.Type.TPA ? "tpa" : "tpahere";

        MessageManager.send(sender, prefix + ".sent", Map.of("player", targetName));
        String acceptCmd = "/tpaccept " + senderName, denyCmd = "/tpdeny " + senderName;
        MessageManager.send(target, prefix + ".header");
        MessageManager.send(target, prefix + ".received", Map.of("player", senderName));
        MessageManager.sendAcceptDenyButtons(target, acceptCmd, denyCmd);
        MessageManager.send(target, prefix + ".footer");
        return true;
    }

    public boolean acceptRequest(ServerPlayer target, String senderName) {
        ServerPlayer sender = PlayerManager.getPlayer(senderName);
        if (sender == null) { MessageManager.sendError(target, "tpa.player_offline"); return false; }

        TeleportRequest r = getPendingRequestFrom(sender, target);
        if (r == null) { MessageManager.sendError(target, "tpa.no_request"); return false; }

        r.setState(TeleportRequest.State.ACCEPTED);
        pendingRequests.remove(r.getId());
        EventManager.getInstance().fire(new TeleportEvents.TpaEvent(sender, target, TeleportEvents.TpaEvent.Type.ACCEPT));

        ServerPlayer tp = r.getTeleportPlayer(), dest = r.getDestinationPlayer();
        int delay = ConfigManager.getInstance().getConfig().teleportDelay;
        String type = r.getType() == TeleportRequest.Type.TPA ? "tpa" : "tpahere";

        MessageManager.sendSuccess(tp, "tpa.accepted_delay",
            Map.of("player", dest.getDisplayName().getString(), "seconds", String.valueOf(delay)));
        MessageManager.sendSuccess(dest, "tpa.accepted_delay",
            Map.of("player", tp.getDisplayName().getString(), "seconds", String.valueOf(delay)));

        scheduler.schedule(() -> server.execute(() -> {
            if (!PlayerManager.isPlayerOnline(tp.getDisplayName().getString()) ||
                !PlayerManager.isPlayerOnline(dest.getDisplayName().getString())) {
                MessageManager.sendError(tp, "tpa.player_offline");
                MessageManager.sendError(dest, "tpa.player_offline");
                return;
            }
            if (doTeleport(tp, dest)) {
                r.setState(TeleportRequest.State.COMPLETED);
                applyCooldown(tp);
                MessageManager.sendSuccess(tp, "tpa.teleporting", Map.of("player", dest.getDisplayName().getString()));
                MessageManager.sendSuccess(dest, "tpa.accepted", Map.of("player", tp.getDisplayName().getString()));
                EventManager.getInstance().fireAsync(new TeleportEvents.TeleportEvent(tp, dest, type));
            } else {
                MessageManager.sendError(tp, "tpa.failed");
                MessageManager.sendError(dest, "tpa.failed");
            }
        }), delay, TimeUnit.SECONDS);
        return true;
    }

    public boolean denyRequest(ServerPlayer target, String senderName) {
        ServerPlayer sender = PlayerManager.getPlayer(senderName);
        if (sender == null) { MessageManager.sendError(target, "tpa.player_offline"); return false; }
        TeleportRequest r = getPendingRequestFrom(sender, target);
        if (r == null) { MessageManager.sendError(target, "tpa.no_request"); return false; }

        r.setState(TeleportRequest.State.DENIED);
        pendingRequests.remove(r.getId());
        EventManager.getInstance().fire(new TeleportEvents.TpaEvent(sender, target, TeleportEvents.TpaEvent.Type.DENY));

        MessageManager.send(sender, "tpa.denied_by_player", Map.of("player", target.getDisplayName().getString()));
        MessageManager.sendSuccess(target, "tpa.denied", Map.of("player", sender.getDisplayName().getString()));
        return true;
    }

    // ==================== Back ====================

    public boolean back(ServerPlayer player) {
        if (!checkCooldown(player)) return false;
        GlobalPos deathPos = PlayerManager.getLastDeathPosition(player);
        if (deathPos != null) {
            ServerLevel level = server.getLevel(deathPos.dimension());
            if (level != null) {
                MessageManager.send(player, "back.header");
                double x = deathPos.pos().getX() + 0.5, y = deathPos.pos().getY() + 0.05, z = deathPos.pos().getZ() + 0.5;
                teleportAsync(player, level, x, y, z,
                    () -> {
                        applyCooldown(player);
                        MessageManager.sendSuccess(player, "back.success");
                        MessageManager.send(player, "back.footer");
                        EventManager.getInstance().fireAsync(new TeleportEvents.BackEvent(player));
                    },
                    () -> {
                        MessageManager.sendError(player, "home.failed");
                        MessageManager.send(player, "back.footer");
                    });
                return true;
            }
        }
        MessageManager.sendError(player, "back.no_location");
        return false;
    }

    // ==================== Home ====================

    public boolean setHome(ServerPlayer player) {
        PlayerDataManager.getInstance().setHome(player.getUUID(), player);
        PlayerDataManager.getInstance().save();
        MessageManager.send(player, "home.header");
        MessageManager.sendSuccess(player, "home.set");
        MessageManager.send(player, "home.footer");
        EventManager.getInstance().fireAsync(new TeleportEvents.HomeEvent(player, "home", TeleportEvents.HomeEvent.Type.SET));
        return true;
    }

    public boolean deleteHome(ServerPlayer player) {
        if (PlayerDataManager.getInstance().getHome(player.getUUID()) == null) {
            MessageManager.sendError(player, "home.not_found");
            return false;
        }
        PlayerDataManager.getInstance().deleteHome(player.getUUID());
        PlayerDataManager.getInstance().save();
        MessageManager.send(player, "home.header");
        MessageManager.sendSuccess(player, "home.deleted");
        MessageManager.send(player, "home.footer");
        return true;
    }

    public boolean home(ServerPlayer player) {
        if (!checkCooldown(player)) return false;
        PlayerDataManager.HomeData h = PlayerDataManager.getInstance().getHome(player.getUUID());
        if (h == null) return setHome(player);

        GlobalPos pos = h.toGlobalPos(server);
        if (pos == null) { MessageManager.sendError(player, "home.failed"); return false; }

        ServerLevel level = server.getLevel(pos.dimension());
        if (level == null) { MessageManager.sendError(player, "home.failed"); return false; }

        MessageManager.send(player, "home.header");
        teleportAsync(player, level, pos.pos().getX() + 0.5, pos.pos().getY(), pos.pos().getZ() + 0.5,
            () -> {
                applyCooldown(player);
                MessageManager.sendSuccess(player, "home.teleported");
                MessageManager.send(player, "home.footer");
                EventManager.getInstance().fireAsync(new TeleportEvents.HomeEvent(player, "home", TeleportEvents.HomeEvent.Type.TELEPORT));
            },
            () -> {
                MessageManager.sendError(player, "home.failed");
                MessageManager.send(player, "home.footer");
            });
        return true;
    }

    // ==================== Admin ====================

    public boolean tpAll(ServerPlayer admin) {
        MessageManager.send(admin, "tpall.header");
        int count = 0;
        String adminName = admin.getDisplayName().getString();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p != admin) { doTeleport(p, admin); count++; }
        }
        MessageManager.sendSuccess(admin, "tpall.success", Map.of("count", String.valueOf(count)));
        for (ServerPlayer p : server.getPlayerList().getPlayers())
            if (p != admin) MessageManager.send(p, "tpall.notify", Map.of("player", adminName));
        MessageManager.send(admin, "tpall.footer");
        return true;
    }

    // ==================== Helpers ====================

    private boolean checkCooldown(ServerPlayer player) {
        Long last = cooldowns.get(player.getUUID());
        if (last != null) {
            int cd = ConfigManager.getInstance().getConfig().teleportCooldown;
            long elapsed = System.currentTimeMillis() - last;
            if (elapsed < cd * 1000L) {
                MessageManager.sendError(player, "cooldown.active", Map.of("seconds", String.valueOf(cd - elapsed / 1000)));
                return false;
            }
        }
        return true;
    }

    private void applyCooldown(ServerPlayer player) { cooldowns.put(player.getUUID(), System.currentTimeMillis()); }

    private TeleportRequest getPendingRequestFrom(ServerPlayer sender, ServerPlayer target) {
        for (TeleportRequest r : pendingRequests.values())
            if (r.getSender().getUUID().equals(sender.getUUID()) &&
                r.getTarget().getUUID().equals(target.getUUID()) &&
                r.getState() == TeleportRequest.State.PENDING) return r;
        return null;
    }

    private void startTimeoutChecker() {
        scheduler.scheduleAtFixedRate(() -> server.execute(() -> {
            int timeout = ConfigManager.getInstance().getConfig().teleportTimeout;
            var it = pendingRequests.values().iterator();
            while (it.hasNext()) {
                TeleportRequest r = it.next();
                if (r.isExpired(timeout)) {
                    r.setState(TeleportRequest.State.TIMEOUT); it.remove();
                    MessageManager.sendError(r.getSender(), "tpa.timeout", Map.of("player", r.getTarget().getDisplayName().getString()));
                    MessageManager.sendError(r.getTarget(), "tpa.timeout_target", Map.of("player", r.getSender().getDisplayName().getString()));
                }
            }
        }), 1, 1, TimeUnit.SECONDS);
    }
}
