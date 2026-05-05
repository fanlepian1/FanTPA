package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.config.ConfigData;
import cn.fancraft.fantpa.teleport.TeleportHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
                .executes(HomeCommands::goHome));

        if (cfg.sethomeEnabled)
            dispatcher.register(Commands.literal("sethome")
                .requires(src -> src.getPlayer() != null)
                .executes(HomeCommands::setHome));

        if (cfg.delhomeEnabled)
            dispatcher.register(Commands.literal("delhome")
                .requires(src -> src.getPlayer() != null)
                .executes(HomeCommands::deleteHome));
    }

    private static int goHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().home(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int setHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().setHome(player);
        return Command.SINGLE_SUCCESS;
    }

    private static int deleteHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().deleteHome(player);
        return Command.SINGLE_SUCCESS;
    }
}
