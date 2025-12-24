package top.yunmouren.craftbrowser.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import top.yunmouren.craftbrowser.server.command.CommonCommand;

public class ForgeCommand {

    private static final CommonCommand COMMON_COMMAND = new CommonCommand();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(COMMON_COMMAND.buildCommandTree());
    }
}

