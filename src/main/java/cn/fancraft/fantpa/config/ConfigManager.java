package cn.fancraft.fantpa.config;

import cn.fancraft.fantpa.utils.LoggerUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();
    private static final Path CONFIG_DIR = Paths.get("config", "fantpa");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("fantpa.conf");

    private ConfigData config;

    public static ConfigManager getInstance() { return INSTANCE; }

    public ConfigData getConfig() { return config; }

    public void loadConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (Files.notExists(CONFIG_FILE)) {
                config = new ConfigData();
                saveConfig();
                LoggerUtil.info("已生成默认配置文件");
                return;
            }
            config = ConfigParser.parse(Files.readString(CONFIG_FILE));
            LoggerUtil.info("配置文件加载成功");
        } catch (IOException e) {
            LoggerUtil.error("配置文件加载失败，使用默认配置", e);
            config = new ConfigData();
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public void saveConfig() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(CONFIG_FILE, ConfigParser.serialize(config));
        } catch (IOException e) {
            LoggerUtil.error("配置文件保存失败", e);
        }
    }

    public CompletableFuture<Void> saveConfigAsync() {
        return CompletableFuture.runAsync(this::saveConfig);
    }
}
