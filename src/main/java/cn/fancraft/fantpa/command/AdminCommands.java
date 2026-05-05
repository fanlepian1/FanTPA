package cn.fancraft.fantpa.command;

import cn.fancraft.fantpa.utils.PlayerManager;
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
                    ServerPlayer p = src.getPlayer();
                    return p != null && PlayerManager.isPlayerOP(p.getDisplayName().getString());
                } catch (Exception ignored) { return false; }
            })
            .executes(ctx -> {
                TeleportHandler.getInstance().tpAll(ctx.getSource().getPlayerOrException());
                return Command.SINGLE_SUCCESS;
            }));
    }
}
