package top.yunmouren.fabric;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import top.yunmouren.craftbrowser.server.command.CommonCommand;

public class FabricCommand {

    private static final CommonCommand COMMON_COMMAND = new CommonCommand();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(COMMON_COMMAND.buildCommandTree());
        });
    }
}