package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.config.ConfigManager;
import cn.fancraft.fantpa.teleport.TeleportHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class BackCommand {

    private BackCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (ConfigManager.getInstance().getConfig().backEnabled) {
            dispatcher.register(Commands.literal("back")
                .requires(src -> src.getPlayer() != null)
                .executes(BackCommand::executeBack));
        }
    }

    private static int executeBack(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        TeleportHandler.getInstance().back(ctx.getSource().getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }
}
