package top.yunmouren.craftbrowser.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;

import java.util.Objects;

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
                .requires(src -> src.hasPermission(2))
                .then(playerCommand(CommandType.OPEN_GUI))
                .then(playerCommandWithArg(CommandType.LOAD_URL, "url"));
    }

    /**
     * 构建无参数的玩家命令
     */
    private ArgumentBuilder<CommandSourceStack, ?> playerCommand(CommandType commandType) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(commandType.getCommandName())
                        .executes(ctx -> executePlayerCommand(ctx, commandType))
                );
    }

    /**
     * 构建带参数的玩家命令
     */
    private ArgumentBuilder<CommandSourceStack, ?> playerCommandWithArg(CommandType commandType, String argName) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(commandType.getCommandName())
                        .then(Commands.argument(argName, StringArgumentType.string())
                                .executes(ctx -> {
                                    String value = StringArgumentType.getString(ctx, argName);
                                    return executePlayerCommandWithArg(ctx, commandType, value);
                                })
                        )
                );
    }

    /**
     * 执行无参数命令
     */
    private int executePlayerCommand(CommandContext<CommandSourceStack> ctx, CommandType commandType) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "PlayerName");

        if (Objects.requireNonNull(commandType) == CommandType.OPEN_GUI) {
            networkHandler.sendOpenGui(targetPlayer);
        } else {
            return 0;
        }

        return 1;
    }

    /**
     * 执行带参数的命令
     */
    private int executePlayerCommandWithArg(CommandContext<CommandSourceStack> ctx, CommandType commandType, String arg) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "PlayerName");

        if (Objects.requireNonNull(commandType) == CommandType.LOAD_URL) {
            networkHandler.sendLoadUrl(targetPlayer, arg);
        } else {
            return 0;
        }

        return 1;
    }
}

