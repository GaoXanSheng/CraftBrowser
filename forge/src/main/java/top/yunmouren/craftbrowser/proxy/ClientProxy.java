package top.yunmouren.craftbrowser.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.server.network.BrowserNetworkHandler;
import top.yunmouren.httpserver.HttpNetworkHandler;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        super.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }
    @Override
    protected void onCommonSetup(final FMLCommonSetupEvent event) {
        Craftbrowser.LOGGER.info("Client register packets");
        HttpNetworkHandler.registerS2CReceivers();
        BrowserNetworkHandler.getInstance().registerClientReceiver();
    }
    private void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("fancymenu")) {
            de.keksuccino.fancymenu.customization.background.MenuBackgroundRegistry
                    .register(new top.yunmouren.craftbrowser.fancymenu.BrowserMenuBackgroundBuilder("NCEF"));
        }

    }
}