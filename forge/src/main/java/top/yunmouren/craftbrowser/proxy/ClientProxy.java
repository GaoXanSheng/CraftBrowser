package top.yunmouren.craftbrowser.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }
}