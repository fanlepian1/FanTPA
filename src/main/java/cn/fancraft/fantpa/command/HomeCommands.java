package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.config.ConfigData;
import cn.fancraft.fantpa.teleport.TeleportHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class HomeCommands {

    private HomeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ConfigData cfg = ConfigManager.getInstance().getConfig();

        if (cfg.homeEnabled)
            dispatcher.register(Commands.literal("home")
                .requires(src -> src.getPlayer() != null)
                .executes(ctx -> exec(ctx, null, false))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> exec(ctx, StringArgumentType.getString(ctx, "name"), false))));

        if (cfg.sethomeEnabled)
            dispatcher.register(Commands.literal("sethome")
                .requires(src -> src.getPlayer() != null)
                .executes(ctx -> exec(ctx, null, true))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> exec(ctx, StringArgumentType.getString(ctx, "name"), true))));

        if (cfg.delhomeEnabled)
            dispatcher.register(Commands.literal("delhome")
                .requires(src -> src.getPlayer() != null)
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> delete(ctx, StringArgumentType.getString(ctx, "name")))));
    }

    private static int exec(CommandContext<CommandSourceStack> ctx, String name, boolean set) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (set) TeleportHandler.getInstance().setHome(player, name);
        else TeleportHandler.getInstance().home(player, name);
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(CommandContext<CommandSourceStack> ctx, String homeName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().deleteHome(player, homeName);
        return Command.SINGLE_SUCCESS;
    }
}
