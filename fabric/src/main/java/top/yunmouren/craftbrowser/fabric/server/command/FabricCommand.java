package top.yunmouren.craftbrowser.fabric.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.fabric.server.network.FabricNetworkHandler;
import top.yunmouren.craftbrowser.server.network.NetworkMessageType;

public class FabricCommand {

    public static void register() {
        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("ncef")
                            .requires(src -> src.hasPermission(2)) // 权限等级
                            .then(playerCommand("openDev", NetworkMessageType.OPEN_DEV))
                            .then(playerCommandWithArg("loadUrl", "url", NetworkMessageType.LOAD_URL))
            );
        });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> playerCommand(String name, NetworkMessageType type) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(ctx -> execute(ctx, type, null))
                );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> playerCommandWithArg(String name, String argName, NetworkMessageType type) {
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

    private static int execute(CommandContext<CommandSourceStack> ctx, NetworkMessageType type, String body) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(ctx, "PlayerName");
            FabricNetworkHandler.sendToPlayer(player, type, body);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }
}
