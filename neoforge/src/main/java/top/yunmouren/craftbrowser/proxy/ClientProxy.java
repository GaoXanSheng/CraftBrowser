    package top.yunmouren.craftbrowser.proxy;

    import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
    import net.neoforged.bus.api.IEventBus;
    import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
    import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
    import top.yunmouren.browserblock.ModBlocks;
    import top.yunmouren.browserblock.client.BrowserMasterBlockRenderer;
    import top.yunmouren.craftbrowser.server.network.NetworkRegistry;
    import top.yunmouren.craftbrowser.server.network.packet.HttpRequestPacket;
    import top.yunmouren.craftbrowser.server.network.packet.SetBrowserUrlPacket;

    public final class ClientProxy {
        public static void init(IEventBus modBus) {
            modBus.addListener(ClientProxy::onClientSetup);
            modBus.addListener(ClientProxy::onCommonSetup);
        }

        private static void onClientSetup(final FMLClientSetupEvent event) {
            BlockEntityRendererRegistry.register(
                    ModBlocks.BROWSER_MASTER_ENTITY.get(),
                    BrowserMasterBlockRenderer::new
            );

        }
        private static void onCommonSetup(FMLCommonSetupEvent event) {
            NetworkRegistry.register(SetBrowserUrlPacket.class, SetBrowserUrlPacket.TYPE);
            NetworkRegistry.register(HttpRequestPacket.class, HttpRequestPacket.TYPE);
        }


    }
