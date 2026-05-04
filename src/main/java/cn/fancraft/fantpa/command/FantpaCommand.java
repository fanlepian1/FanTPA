package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.Fantpa;
import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.config.ConfigData;
import cn.fancraft.fantpa.lang.LangManager;
import cn.fancraft.fantpa.message.MessageManager;
import cn.fancraft.fantpa.teleport.TeleportHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class FantpaCommand {

    private FantpaCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("fantpa")
            .requires(src -> {
                try {
                    ServerPlayer player = src.getPlayer();
                    if (player != null) {
                        return cn.fancraft.fantpa.api.PlayerManager.isPlayerOP(player.getDisplayName().getString());
                    }
                } catch (Exception ignored) {}
                return false;
            });

        root.then(Commands.literal("reload")
            .executes(FantpaCommand::executeReload));

        root.then(Commands.literal("version")
            .executes(FantpaCommand::executeVersion));

        root.then(Commands.literal("save")
            .executes(FantpaCommand::executeSave));

        root.then(Commands.literal("set")
            .then(Commands.argument("key", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    builder.suggest("timeout");
                    builder.suggest("cooldown");
                    builder.suggest("delay");
                    return builder.buildFuture();
                })
                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                    .executes(FantpaCommand::executeSet))));

        dispatcher.register(root);
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        ConfigManager.getInstance().reloadConfig();
        LangManager.getInstance().reload();
        sendFeedback(ctx, "fantpa.reloaded");
        return Command.SINGLE_SUCCESS;
    }

    private static int executeVersion(CommandContext<CommandSourceStack> ctx) {
        Map<String, String> ph = Map.of(
            "version", Fantpa.MOD_VERSION,
            "name", Fantpa.MOD_NAME
        );
        sendFeedback(ctx, "fantpa.version", ph);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeSave(CommandContext<CommandSourceStack> ctx) {
        ConfigManager.getInstance().saveConfig();
        TeleportHandler.getInstance().saveHomes();
        sendFeedback(ctx, "fantpa.saved");
        return Command.SINGLE_SUCCESS;
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx) {
        String key = StringArgumentType.getString(ctx, "key");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        ConfigData cfg = ConfigManager.getInstance().getConfig();

        switch (key.toLowerCase()) {
            case "timeout" -> cfg.teleportTimeout = value;
            case "cooldown" -> cfg.teleportCooldown = value;
            case "delay" -> cfg.teleportDelay = value;
            default -> {
                sendFeedback(ctx, "fantpa.unknown_key");
                return 0;
            }
        }

        ConfigManager.getInstance().saveConfigAsync();
        Map<String, String> ph = Map.of("key", key, "value", String.valueOf(value));
        sendFeedback(ctx, "fantpa.set", ph);
        return Command.SINGLE_SUCCESS;
    }

    private static void sendFeedback(CommandContext<CommandSourceStack> ctx, String key) {
        sendFeedback(ctx, key, null);
    }

    private static void sendFeedback(CommandContext<CommandSourceStack> ctx, String key, Map<String, String> ph) {
        ServerPlayer player = null;
        try { player = ctx.getSource().getPlayer(); } catch (Exception ignored) {}
        if (player != null) {
            MessageManager.sendSuccess(player, key, ph);
        } else {
            String msg = LangManager.getInstance().get(key, ph);
            String plain = msg.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");
            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal(plain), false);
        }
    }
}
