package top.yunmouren.craftbrowser.fabric.client;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import top.yunmouren.browserblock.client.BrowserBlockRenderer;

import static top.yunmouren.browserblock.registry.ModBlocks.BROWSER_BLOCK_ENTITY;


public final class CraftbrowserFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(BROWSER_BLOCK_ENTITY.get(), BrowserBlockRenderer::new);
    }
}
