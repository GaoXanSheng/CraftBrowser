package top.yunmouren.craftbrowser.proxy;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent; // 导入这个
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.browserblock.client.BrowserBlockRenderer;

import static top.yunmouren.browserblock.ModBlocks.BROWSER_BLOCK_ENTITY;


@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        BlockEntityRendererRegistry.register(BROWSER_BLOCK_ENTITY.get(), BrowserBlockRenderer::new);
    }
}