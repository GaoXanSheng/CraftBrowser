package top.yunmouren.craftbrowser.proxy;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.browserblock.client.BrowserMasterBlockRenderer;
import top.yunmouren.browserblock.ModBlocks;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        BlockEntityRendererRegistry.register(ModBlocks.BROWSER_MASTER_ENTITY.get(), BrowserMasterBlockRenderer::new);
    }
}