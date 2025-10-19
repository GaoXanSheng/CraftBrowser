package top.yunmouren.craftbrowser.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import top.yunmouren.craftbrowser.server.ForgeNetworkHandler;
import top.yunmouren.craftbrowser.server.network.NetworkMessageType;

public class ForgeCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ncef")
                        .then(playerCommand("openDev", NetworkMessageType.OPEN_DEV))
                        .then(playerCommandWithArg("loadUrl", "url", NetworkMessageType.LOAD_URL))
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> playerCommand(String name, NetworkMessageType type) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(name)
                        .executes(ctx -> executeWithOpCheck(ctx, type, null))
                );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> playerCommandWithArg(String name, String argName, NetworkMessageType type) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(name)
                        .then(Commands.argument(argName, StringArgumentType.string())
                                .executes(ctx -> {
                                    String value = StringArgumentType.getString(ctx, argName);
                                    return executeWithOpCheck(ctx, type, value);
                                })
                        )
                );
    }

    // 核心执行方法，带 OP 检查
    private static int executeWithOpCheck(CommandContext<CommandSourceStack> ctx, NetworkMessageType type, String body) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        // 判断执行者是否为 OP
        if (src.getEntity() instanceof ServerPlayer executor) {
            if (!executor.getServer().getPlayerList().isOp(executor.getGameProfile())) {
                src.sendFailure(Component.literal("§c You Do Not Have Permission To Execute This Command！"));
                return 0; // 阻止执行
            }
        }

        // 获取目标玩家并发送网络消息
        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "PlayerName");
        ForgeNetworkHandler.sendToPlayer(targetPlayer, type, body);
        return 1;
    }
}
