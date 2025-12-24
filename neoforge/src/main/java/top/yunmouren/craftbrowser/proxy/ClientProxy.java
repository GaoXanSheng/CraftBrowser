package top.yunmouren.craftbrowser.proxy;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.browserblock.client.BrowserMasterBlockRenderer;
import top.yunmouren.browserblock.network.BrowserBlockNetworkHandler;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;
import top.yunmouren.httpserver.HttpNetworkHandler;

public final class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();

        IEventBus modBus = ModLoadingContext
                .get()
                .getActiveContainer()
                .getEventBus();

        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {

        BlockEntityRendererRegistry.register(
                ModBlocks.BROWSER_MASTER_ENTITY.get(),
                BrowserMasterBlockRenderer::new
        );

        HttpNetworkHandler.registerC2SReceivers();
        BrowserBlockNetworkHandler.registerC2SReceivers();
    }
}
