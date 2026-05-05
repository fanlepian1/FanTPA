package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.utils.PlayerManager;
import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.config.ConfigData;
import cn.fancraft.fantpa.message.MessageManager;
import cn.fancraft.fantpa.teleport.TeleportHandler;
import cn.fancraft.fantpa.teleport.TeleportRequest;
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

        if (cfg.tpaEnabled) registerTpaCommand(dispatcher, "tpa", TeleportRequest.Type.TPA);
        if (cfg.tpahereEnabled) registerTpaCommand(dispatcher, "tpahere", TeleportRequest.Type.TPAHERE);

        dispatcher.register(Commands.literal("tpaccept")
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    if (ctx.getSource().getPlayer() != null) {
                        for (var e : TeleportHandler.getInstance().pendingRequests().entrySet())
                            if (e.getValue().getTarget().getUUID().equals(ctx.getSource().getPlayer().getUUID()))
                                builder.suggest(e.getValue().getSender().getDisplayName().getString());
                    }
                    return builder.buildFuture();
                })
                .executes(TpaCommands::executeTpAccept)));

        dispatcher.register(Commands.literal("tpdeny")
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    if (ctx.getSource().getPlayer() != null) {
                        for (var e : TeleportHandler.getInstance().pendingRequests().entrySet())
                            if (e.getValue().getTarget().getUUID().equals(ctx.getSource().getPlayer().getUUID()))
                                builder.suggest(e.getValue().getSender().getDisplayName().getString());
                    }
                    return builder.buildFuture();
                })
                .executes(TpaCommands::executeTpDeny)));
    }

    private static void registerTpaCommand(CommandDispatcher<CommandSourceStack> dispatcher, String name, TeleportRequest.Type type) {
        dispatcher.register(Commands.literal(name)
            .requires(src -> true)
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    for (var p : ctx.getSource().getServer().getPlayerList().getPlayers())
                        builder.suggest(p.getDisplayName().getString());
                    return builder.buildFuture();
                })
                .executes(ctx -> executeTpa(ctx, type))));
    }

    private static int executeTpa(CommandContext<CommandSourceStack> ctx, TeleportRequest.Type type) throws CommandSyntaxException {
        ServerPlayer sender = ctx.getSource().getPlayerOrException();
        String targetName = StringArgumentType.getString(ctx, "player");
        String prefix = type == TeleportRequest.Type.TPA ? "tpa" : "tpahere";

        if (sender.getDisplayName().getString().equalsIgnoreCase(targetName)) {
            MessageManager.sendError(sender, prefix + ".self");
            return 0;
        }
        if (!PlayerManager.isPlayerOnline(targetName)) {
            MessageManager.sendError(sender, "tpa.player_offline");
            return 0;
        }
        TeleportHandler.getInstance().requestTeleport(sender, PlayerManager.getPlayer(targetName), type);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTpAccept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().acceptRequest(target, StringArgumentType.getString(ctx, "player"));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTpDeny(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().denyRequest(target, StringArgumentType.getString(ctx, "player"));
        return Command.SINGLE_SUCCESS;
    }

}
