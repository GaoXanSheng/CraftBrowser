package top.yunmouren.craftbrowser.fabric.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import top.yunmouren.browserblock.ModBlocks;
import top.yunmouren.browserblock.client.BrowserMasterBlockRenderer;


public final class CraftbrowserFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModBlocks.BROWSER_MASTER_ENTITY.get(), BrowserMasterBlockRenderer::new);
    }
}
