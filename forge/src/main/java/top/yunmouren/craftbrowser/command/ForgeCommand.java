package top.yunmouren.craftbrowser.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import top.yunmouren.craftbrowser.server.command.CommonCommand;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;

/**
 * Forge平台命令注册
 */
public class ForgeCommand {

    private static final CommonCommand COMMON_COMMAND = new CommonCommand(BrowserNetworkHandler.getInstance());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(COMMON_COMMAND.buildCommandTree());
    }
}

