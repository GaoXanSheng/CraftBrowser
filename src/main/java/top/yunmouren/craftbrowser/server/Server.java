package top.yunmouren.craftbrowser.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.server.command.Command;
import top.yunmouren.craftbrowser.server.network.NetworkHandler;

import java.util.Optional;

import static top.yunmouren.craftbrowser.server.network.NetworkHandler.CHANNEL;

public class Server {
    // -----------------------
    // 网络部分
    // -----------------------
    private static final String PROTOCOL_VERSION = "1.0";
    private static int packetId = 0;


    public Server() {
        // 生命周期事件（mod 阶段）
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

        // Forge 游戏事件（运行时）
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    // -----------------------
    // 生命周期事件
    // -----------------------
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        Craftbrowser.LOGGER.info("Server setup: register packets");
        registerPackets();
    }

    // -----------------------
    // 网络注册
    // -----------------------
    private void registerPackets() {
        NetworkHandler.register();
    }

    private static int nextId() {
        return packetId++;
    }

    // -----------------------
    // 命令注册
    // -----------------------
    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        Command.register(dispatcher);
    }
}
