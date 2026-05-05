package cn.fancraft.fantpa.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private static final PlayerDataManager INSTANCE = new PlayerDataManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DATA_FILE = Paths.get("config", "fantpa", "data.json");
    private static final Type DATA_TYPE = new TypeToken<Map<String, PlayerData>>() {}.getType();

    private final Map<UUID, PlayerData> data = new ConcurrentHashMap<>();

    public static PlayerDataManager getInstance() { return INSTANCE; }

    public void load() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            if (Files.exists(DATA_FILE)) {
                Map<String, PlayerData> loaded = GSON.fromJson(Files.readString(DATA_FILE), DATA_TYPE);
                if (loaded != null)
                    for (var e : loaded.entrySet())
                        try { data.put(UUID.fromString(e.getKey()), e.getValue()); }
                        catch (IllegalArgumentException ignored) {}
                LoggerUtil.info("Player data loaded: " + data.size() + " entries");
            }
        } catch (IOException e) { LoggerUtil.error("Failed to load player data", e); }
    }

    public void save() {
        try {
            Files.createDirectories(DATA_FILE.getParent());
            Files.writeString(DATA_FILE, GSON.toJson(data));
        } catch (IOException e) { LoggerUtil.error("Failed to save player data", e); }
    }

    // ---- Home ----

    public HomeData getHome(UUID uuid) {
        PlayerData pd = data.get(uuid);
        return pd != null ? pd.home : null;
    }

    public void setHome(UUID uuid, ServerPlayer player) {
        data.computeIfAbsent(uuid, k -> new PlayerData()).home = new HomeData(player);
    }

    public void deleteHome(UUID uuid) {
        PlayerData pd = data.get(uuid);
        if (pd != null) pd.home = null;
    }

    // ---- Language ----

    public String getLanguage(UUID uuid) {
        PlayerData pd = data.get(uuid);
        return pd != null ? pd.language : null;
    }

    public void setLanguage(UUID uuid, String language) {
        data.computeIfAbsent(uuid, k -> new PlayerData()).language = language;
    }

    // ---- Data classes ----

    public static class PlayerData {
        HomeData home;
        String language;
    }

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
            for (ServerLevel level : server.getAllLevels())
                if (level.dimension().toString().equals(dimension))
                    return GlobalPos.of(level.dimension(), new BlockPos(x, y, z));
            return null;
        }
    }
}
