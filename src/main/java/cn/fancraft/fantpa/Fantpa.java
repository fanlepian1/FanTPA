package cn.fancraft.fantpa;

import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.lang.LangManager;
import cn.fancraft.fantpa.event.EventManager;
import cn.fancraft.fantpa.teleport.TeleportHandler;
import cn.fancraft.fantpa.command.ModCommands;
import cn.fancraft.fantpa.utils.LoggerUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class Fantpa implements ModInitializer {

    public static final String MOD_ID = "fantpa";
    public static final String MOD_NAME = "FanTPA";
    public static final String MOD_VERSION = "1.0.0";

    public static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        ConfigManager.getInstance().loadConfig();
        LangManager.getInstance().loadLanguages();
        EventManager.getInstance();
        ModCommands.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
            TeleportHandler.getInstance().init(server);
            LoggerUtil.info("FanTPA 所有模块初始化完成");
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ConfigManager.getInstance().saveConfigAsync();
            TeleportHandler.getInstance().shutdown();
            serverInstance = null;
        });
    }

    public static MinecraftServer getServerInstance() {
        return serverInstance;
    }
}
