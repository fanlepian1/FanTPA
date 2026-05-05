package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.Fantpa;
import cn.fancraft.fantpa.utils.PlayerManager;
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

import java.util.Map;

public class FantpaCommand {

    private FantpaCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("fantpa")
            .requires(src -> {
                try {
                    var p = src.getPlayer();
                    return p != null && PlayerManager.isPlayerOP(p.getDisplayName().getString());
                } catch (Exception ignored) { return false; }
            });

        root.then(Commands.literal("reload").executes(FantpaCommand::executeReload));
        root.then(Commands.literal("version").executes(FantpaCommand::executeVersion));
        root.then(Commands.literal("save").executes(FantpaCommand::executeSave));
        root.then(Commands.literal("set")
            .then(Commands.argument("key", StringArgumentType.word())
                .suggests((ctx, b) -> { b.suggest("timeout"); b.suggest("cooldown"); b.suggest("delay"); return b.buildFuture(); })
                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                    .executes(FantpaCommand::executeSet))));

        dispatcher.register(root);
    }

    private static int executeReload(CommandContext<CommandSourceStack> ctx) {
        ConfigManager.getInstance().reloadConfig();
        LangManager.getInstance().reload();
        MessageManager.sendFeedback(ctx.getSource(), "fantpa.reloaded");
        return Command.SINGLE_SUCCESS;
    }

    private static int executeVersion(CommandContext<CommandSourceStack> ctx) {
        MessageManager.sendFeedback(ctx.getSource(), "fantpa.version", Map.of("version", Fantpa.MOD_VERSION, "name", Fantpa.MOD_NAME));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeSave(CommandContext<CommandSourceStack> ctx) {
        ConfigManager.getInstance().saveConfig();
        TeleportHandler.getInstance().saveHomes();
        MessageManager.sendFeedback(ctx.getSource(), "fantpa.saved");
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
                MessageManager.sendFeedback(ctx.getSource(), "fantpa.unknown_key");
                return 0;
            }
        }
        ConfigManager.getInstance().saveConfigAsync();
        MessageManager.sendFeedback(ctx.getSource(), "fantpa.set", Map.of("key", key, "value", String.valueOf(value)));
        return Command.SINGLE_SUCCESS;
    }
}
