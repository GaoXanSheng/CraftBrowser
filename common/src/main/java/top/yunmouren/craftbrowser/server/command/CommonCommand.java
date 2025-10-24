package top.yunmouren.craftbrowser.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;

/**
 * 通用命令注册类
 * 提供跨平台的命令构建逻辑
 */
public record CommonCommand(BrowserNetworkHandler networkHandler) {

    /**
     * 构建命令树
     */
    public LiteralArgumentBuilder<CommandSourceStack> buildCommandTree() {
        return Commands.literal("ncef")
                .then(playerCommand(CommandType.OPEN_GUI))
                .then(playerCommandWithArg(CommandType.LOAD_URL, "url"));
    }

    /**
     * 带权限检查的命令树（Fabric使用）
     */
    public LiteralArgumentBuilder<CommandSourceStack> buildCommandTreeWithPermission() {
        return Commands.literal("ncef")
                .requires(src -> src.hasPermission(2)) // 权限等级
                .then(playerCommand(CommandType.OPEN_GUI))
                .then(playerCommandWithArg(CommandType.LOAD_URL, "url"));
    }

    private ArgumentBuilder<CommandSourceStack, ?> playerCommand(CommandType commandType) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(commandType.getCommandName())
                        .executes(ctx -> executeOpenDev(ctx))
                );
    }

    private ArgumentBuilder<CommandSourceStack, ?> playerCommandWithArg(CommandType commandType, String argName) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(commandType.getCommandName())
                        .then(Commands.argument(argName, StringArgumentType.string())
                                .executes(ctx -> {
                                    String value = StringArgumentType.getString(ctx, argName);
                                    return executeLoadUrl(ctx, value);
                                })
                        )
                );
    }

    /**
     * 检查执行者是否为 OP
     * @return true 如果有权限，false 如果没有权限
     */
    private boolean checkOpPermission(CommandSourceStack src) {
        if (src.getEntity() instanceof ServerPlayer executor) {
            if (!executor.getServer().getPlayerList().isOp(executor.getGameProfile())) {
                src.sendFailure(Component.literal("§c You Do Not Have Permission To Execute This Command！"));
                return false;
            }
        }
        return true;
    }

    /**
     * 执行打开开发者工具命令
     */
    private int executeOpenDev(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        // 判断执行者是否为 OP
        if (!checkOpPermission(src)) {
            return 0;
        }

        // 获取目标玩家并发送网络消息
        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "PlayerName");
        networkHandler.sendOpenDev(targetPlayer);
        return 1;
    }

    /**
     * 执行加载URL命令
     */
    private int executeLoadUrl(CommandContext<CommandSourceStack> ctx, String url) throws CommandSyntaxException {
        CommandSourceStack src = ctx.getSource();

        // 判断执行者是否为 OP
        if (!checkOpPermission(src)) {
            return 0;
        }

        // 获取目标玩家并发送网络消息
        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "PlayerName");
        networkHandler.sendLoadUrl(targetPlayer, url);
        return 1;
    }
}

