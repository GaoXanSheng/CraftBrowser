package top.yunmouren.craftbrowser.fabric.server.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import top.yunmouren.craftbrowser.server.command.CommonCommand;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;

public class FabricCommand {

    private static final CommonCommand COMMON_COMMAND = new CommonCommand(BrowserNetworkHandler.getInstance());

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(COMMON_COMMAND.buildCommandTree());
        });
    }
}
