package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.Fantpa;
import cn.fancraft.fantpa.utils.PlayerDataManager;
import cn.fancraft.fantpa.utils.PlayerManager;
import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.config.ConfigData;
import cn.fancraft.fantpa.lang.LangManager;
import cn.fancraft.fantpa.message.MessageManager;
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
        var root = Commands.literal("fantpa");

        var reload = Commands.literal("reload").requires(FantpaCommand::isOP).executes(FantpaCommand::executeReload);
        var version = Commands.literal("version").requires(FantpaCommand::isOP).executes(FantpaCommand::executeVersion);
        var save = Commands.literal("save").requires(FantpaCommand::isOP).executes(FantpaCommand::executeSave);
        var set = Commands.literal("set").requires(FantpaCommand::isOP)
            .then(Commands.argument("key", StringArgumentType.word())
                .suggests((ctx, b) -> { b.suggest("timeout"); b.suggest("cooldown"); b.suggest("delay"); return b.buildFuture(); })
                .then(Commands.argument("value", IntegerArgumentType.integer(1))
                    .executes(FantpaCommand::executeSet)));

        var language = Commands.literal("language")
            .then(Commands.argument("lang", StringArgumentType.word())
                .suggests((ctx, b) -> {
                    for (String locale : LangManager.getInstance().getAvailableLocales())
                        b.suggest(locale);
                    return b.buildFuture();
                })
                .executes(FantpaCommand::executeLanguage));

        root.then(reload).then(version).then(save).then(set).then(language);
        dispatcher.register(root);
    }

    private static boolean isOP(CommandSourceStack src) {
        try {
            var p = src.getPlayer();
            return p != null && PlayerManager.isPlayerOP(p.getDisplayName().getString());
        } catch (Exception ignored) { return false; }
    }

    // ---- Admin commands ----

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
        PlayerDataManager.getInstance().save();
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

    // ---- Player commands ----

    private static int executeLanguage(CommandContext<CommandSourceStack> ctx) {
        String lang = StringArgumentType.getString(ctx, "lang");
        if (!LangManager.getInstance().getAvailableLocales().contains(lang)) {
            MessageManager.sendFeedback(ctx.getSource(), "fantpa.language.invalid",
                Map.of("languages", String.join(", ", LangManager.getInstance().getAvailableLocales())));
            return 0;
        }
        var player = ctx.getSource().getPlayer();
        if (player != null) {
            PlayerDataManager.getInstance().setLanguage(player.getUUID(), lang);
            PlayerDataManager.getInstance().save();
        }
        MessageManager.sendFeedback(ctx.getSource(), "fantpa.language.set", Map.of("language", lang));
        return Command.SINGLE_SUCCESS;
    }
}
