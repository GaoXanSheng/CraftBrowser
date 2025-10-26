package top.yunmouren.craftbrowser.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.craftbrowser.Craftbrowser;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("fancymenu")) {
                try {
                    de.keksuccino.fancymenu.customization.background.MenuBackgroundRegistry.register(new top.yunmouren.craftbrowser.fancymenu.BrowserMenuBackgroundBuilder("NCEF"));
                    Craftbrowser.LOGGER.info("Registered FancyMenu background integration");
                } catch (Exception e) {
                    Craftbrowser.LOGGER.error("Failed to register FancyMenu integration", e);
                }
            }
        });
    }
}