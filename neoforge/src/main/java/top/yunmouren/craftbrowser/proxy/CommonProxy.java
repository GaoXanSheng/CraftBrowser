package top.yunmouren.craftbrowser.proxy;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.craftbrowser.command.ForgeCommand;
import top.yunmouren.craftbrowser.server.network.NetworkRegistry;
import top.yunmouren.craftbrowser.server.network.packet.BrowserPacket;
import top.yunmouren.craftbrowser.server.network.packet.HttpResponsePacket;

public class CommonProxy {
    public static void init(IEventBus modBus) {
        ModBlocks.register();
        modBus.addListener(CommonProxy::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(CommonProxy::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommand.register(dispatcher);
    }
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        NetworkRegistry.register(BrowserPacket.class, BrowserPacket.TYPE);
        NetworkRegistry.register(HttpResponsePacket.class, HttpResponsePacket.TYPE);
    }
}
