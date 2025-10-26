package top.yunmouren.craftbrowser.fabric.server.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import top.yunmouren.craftbrowser.server.command.CommonCommand;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;

/**
 * Fabric平台命令注册
 */
public class FabricCommand {

    private static final CommonCommand COMMON_COMMAND = new CommonCommand(BrowserNetworkHandler.getInstance());

    public static void register() {
        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(COMMON_COMMAND.buildCommandTree());
        });
    }
}
