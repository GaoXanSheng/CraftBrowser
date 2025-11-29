package top.yunmouren.craftbrowser.proxy;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent; // 导入这个
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.browserblock.client.BrowserBlockRenderer;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        // 1. 调用父类 init 以确保方块被注册
        super.init();

        // 2. 注册 ClientSetup 事件监听器
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    // 3. 在这里注册渲染器
    private void onClientSetup(final FMLClientSetupEvent event) {
        // 此时注册已经完成，调用 .get() 是安全的
        BlockEntityRendererRegistry.register(CommonProxy.BROWSER_BLOCK_ENTITY.get(), BrowserBlockRenderer::new);
    }
}