package top.yunmouren.fabric.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.browserblock.client.BrowserMasterBlockRenderer;
import top.yunmouren.craftbrowser.server.network.NetworkRegistry;
import top.yunmouren.craftbrowser.server.network.packet.HttpRequestPacket;
import top.yunmouren.craftbrowser.server.network.packet.SetBrowserUrlPacket;

public final class craftbrowserFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkRegistry.register(SetBrowserUrlPacket.class, SetBrowserUrlPacket.TYPE);
        NetworkRegistry.register(HttpRequestPacket.class, HttpRequestPacket.TYPE);
        BlockEntityRendererRegistry.register(
                ModBlocks.BROWSER_MASTER_ENTITY.get(),
                BrowserMasterBlockRenderer::new
        );
    }
}
