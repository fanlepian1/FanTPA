package cn.fancraft.fantpa.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommands {

    private ModCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TpaCommands.register(dispatcher);
            HomeCommands.register(dispatcher);
            BackCommand.register(dispatcher);
            AdminCommands.register(dispatcher);
            FantpaCommand.register(dispatcher);
        });
    }
}
