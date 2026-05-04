package cn.fancraft.fantpa.command;

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

public class HomeCommands {

    private HomeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ConfigData cfg = ConfigManager.getInstance().getConfig();

        if (cfg.homeEnabled) {
            dispatcher.register(Commands.literal("home")
                .requires(src -> src.getPlayer() != null)
                .executes(ctx -> executeHome(ctx, null))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> executeHome(ctx, StringArgumentType.getString(ctx, "name")))));
        }

        if (cfg.sethomeEnabled) {
            dispatcher.register(Commands.literal("sethome")
                .requires(src -> src.getPlayer() != null)
                .executes(ctx -> executeSetHome(ctx, null))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> executeSetHome(ctx, StringArgumentType.getString(ctx, "name")))));
        }

        if (cfg.delhomeEnabled) {
            dispatcher.register(Commands.literal("delhome")
                .requires(src -> src.getPlayer() != null)
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> executeDelHome(ctx, StringArgumentType.getString(ctx, "name")))));
        }
    }

    private static int executeHome(CommandContext<CommandSourceStack> ctx, String homeName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().home(player, homeName);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeSetHome(CommandContext<CommandSourceStack> ctx, String homeName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().setHome(player, homeName);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeDelHome(CommandContext<CommandSourceStack> ctx, String homeName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeleportHandler.getInstance().deleteHome(player, homeName);
        return Command.SINGLE_SUCCESS;
    }
}
