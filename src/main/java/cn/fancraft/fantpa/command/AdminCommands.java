package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.teleport.TeleportHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class AdminCommands {

    private AdminCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpall")
            .requires(src -> {
                try {
                    ServerPlayer player = src.getPlayer();
                    if (player != null)
                        return cn.fancraft.fantpa.api.PlayerManager.isPlayerOP(
                            player.getDisplayName().getString());
                } catch (Exception ignored) {}
                return false;
            })
            .executes(AdminCommands::executeTpAll));
    }

    private static int executeTpAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer admin = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().tpAll(admin);
        return Command.SINGLE_SUCCESS;
    }
}
