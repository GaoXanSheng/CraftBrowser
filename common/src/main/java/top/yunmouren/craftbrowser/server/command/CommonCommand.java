package top.yunmouren.craftbrowser.server.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.networking.NetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import top.yunmouren.craftbrowser.server.network.NetworkRegistry;
import top.yunmouren.craftbrowser.server.network.packet.BrowserPacket;

import java.util.Objects;

/**
 * 通用命令注册类
 * 提供跨平台的命令构建逻辑
 */
public record CommonCommand() {

    public LiteralArgumentBuilder<CommandSourceStack> buildCommandTree() {
        return Commands.literal("ncef")
                .requires(src -> src.hasPermission(2))
                .then(playerCommand(CommandType.OPEN_GUI))
                .then(playerCommandWithArg(CommandType.LOAD_URL, "url"));
    }

    private ArgumentBuilder<CommandSourceStack, ?> playerCommand(CommandType commandType) {
        return Commands.argument("PlayerName", EntityArgument.player())
                .then(Commands.literal(commandType.getCommandName())
                        .executes(ctx -> executePlayerCommand(ctx, commandType))
                );
    }
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

    private int executePlayerCommand(CommandContext<CommandSourceStack> ctx, CommandType commandType) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "PlayerName");

        if (Objects.requireNonNull(commandType) == CommandType.OPEN_GUI) {
            NetworkManager.sendToPlayer(targetPlayer,new BrowserPacket(commandType, null));
        } else {
            return 0;
        }

        return 1;
    }

    private int executePlayerCommandWithArg(CommandContext<CommandSourceStack> ctx, CommandType commandType, String arg) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(ctx, "PlayerName");
        if (Objects.requireNonNull(commandType) == CommandType.LOAD_URL) {
            NetworkManager.sendToPlayer(targetPlayer,new BrowserPacket(commandType, arg));
        } else {
            return 0;
        }
        return 1;
    }
}

