package top.yunmouren.craftbrowser.proxy;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.command.ForgeCommand;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;
import top.yunmouren.httpserver.HttpNetworkHandler;

public class CommonProxy {

    public void init() {
        top.yunmouren.browserblock.ModBlocks.register();

        IEventBus modBus = ModLoadingContext
                .get()
                .getActiveContainer()
                .getEventBus();

        modBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ForgeCommand.register(dispatcher);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        Craftbrowser.LOGGER.info("Common setup: register packets");
        BrowserNetworkHandler.registerS2C();
        HttpNetworkHandler.registerS2CReceivers();
    }
}
