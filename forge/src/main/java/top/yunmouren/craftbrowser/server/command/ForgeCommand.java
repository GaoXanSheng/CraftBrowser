package top.yunmouren.craftbrowser.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.server.ForgeNetworkHandler;
import top.yunmouren.craftbrowser.server.network.NetworkMessageType;


public class ForgeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ncef")
                        .requires(src -> src.hasPermission(2))
                        .then(playerCommand("openDev", NetworkMessageType.OPEN_DEV))
                        .then(playerCommandWithArg("loadUrl", "url", NetworkMessageType.LOAD_URL))
        );
    }

    private static ArgumentBuilder<CommandSourceStack,?> playerCommand(String name, NetworkMessageType type) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(ctx -> execute(ctx, type, null))
                );
    }

    private static ArgumentBuilder<CommandSourceStack,?> playerCommandWithArg(String name, String argName, NetworkMessageType type) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(name)
                        .then(Commands.argument(argName, StringArgumentType.string())
                                .executes(ctx -> {
                                    String value = StringArgumentType.getString(ctx, argName);
                                    return execute(ctx, type, value);
                                })
                        )
                );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, NetworkMessageType type, String body) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "PlayerName");
        ForgeNetworkHandler.sendToPlayer(player, type, body);
        return 1;
    }
}
