package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.api.PlayerManager;
import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.config.ConfigData;
import cn.fancraft.fantpa.message.MessageManager;
import cn.fancraft.fantpa.teleport.TeleportHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class TpaCommands {

    private TpaCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ConfigData cfg = ConfigManager.getInstance().getConfig();

        if (cfg.tpaEnabled) {
            dispatcher.register(Commands.literal("tpa")
                .requires(TpaCommands::hasPermission)
                .then(Commands.argument("player", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (var player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                            builder.suggest(player.getDisplayName().getString());
                        }
                        return builder.buildFuture();
                    })
                    .executes(TpaCommands::executeTpa)));
        }

        if (cfg.tpahereEnabled) {
            dispatcher.register(Commands.literal("tpahere")
                .requires(TpaCommands::hasPermission)
                .then(Commands.argument("player", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        for (var player : ctx.getSource().getServer().getPlayerList().getPlayers()) {
                            builder.suggest(player.getDisplayName().getString());
                        }
                        return builder.buildFuture();
                    })
                    .executes(TpaCommands::executeTpaHere)));
        }

        dispatcher.register(Commands.literal("tpaccept")
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    if (ctx.getSource().getPlayer() != null) {
                        for (var entry : TeleportHandler.getInstance().pendingRequests().entrySet()) {
                            if (entry.getValue().getTarget().getUUID()
                                .equals(ctx.getSource().getPlayer().getUUID())) {
                                builder.suggest(entry.getValue().getSender().getDisplayName().getString());
                            }
                        }
                    }
                    return builder.buildFuture();
                })
                .executes(TpaCommands::executeTpAccept)));

        dispatcher.register(Commands.literal("tpdeny")
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    if (ctx.getSource().getPlayer() != null) {
                        for (var entry : TeleportHandler.getInstance().pendingRequests().entrySet()) {
                            if (entry.getValue().getTarget().getUUID()
                                .equals(ctx.getSource().getPlayer().getUUID())) {
                                builder.suggest(entry.getValue().getSender().getDisplayName().getString());
                            }
                        }
                    }
                    return builder.buildFuture();
                })
                .executes(TpaCommands::executeTpDeny)));
    }

    private static int executeTpa(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        String targetName = StringArgumentType.getString(ctx, "player");

        if (sender.getDisplayName().getString().equalsIgnoreCase(targetName)) {
            MessageManager.sendError(sender, "tpa.self");
            return 0;
        }

        if (!PlayerManager.isPlayerOnline(targetName)) {
            MessageManager.sendError(sender, "tpa.player_offline");
            return 0;
        }

        ServerPlayer target = PlayerManager.getPlayer(targetName);
        TeleportHandler.getInstance().requestTpa(sender, target);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTpaHere(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        String targetName = StringArgumentType.getString(ctx, "player");

        if (sender.getDisplayName().getString().equalsIgnoreCase(targetName)) {
            MessageManager.sendError(sender, "tpahere.self");
            return 0;
        }

        if (!PlayerManager.isPlayerOnline(targetName)) {
            MessageManager.sendError(sender, "tpa.player_offline");
            return 0;
        }

        ServerPlayer target = PlayerManager.getPlayer(targetName);
        TeleportHandler.getInstance().requestTpaHere(sender, target);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTpAccept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = ctx.getSource().getPlayerOrException();
        String senderName = StringArgumentType.getString(ctx, "player");
        TeleportHandler.getInstance().acceptRequest(target, senderName);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTpDeny(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = ctx.getSource().getPlayerOrException();
        String senderName = StringArgumentType.getString(ctx, "player");
        TeleportHandler.getInstance().denyRequest(target, senderName);
        return Command.SINGLE_SUCCESS;
    }

    private static boolean hasPermission(CommandSourceStack src) {
        return true;
    }
}
