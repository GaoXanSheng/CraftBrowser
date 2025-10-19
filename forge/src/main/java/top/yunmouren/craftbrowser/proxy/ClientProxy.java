package top.yunmouren.craftbrowser.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.server.Server;
import top.yunmouren.httpserver.HttpNetworkHandler;

@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IProxy {
    @Override
    public void init() {
        new Server();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        Craftbrowser.LOGGER.info("Client register packets");
        HttpNetworkHandler.registerS2CReceivers();
    }
    private void onClientSetup(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("fancymenu")) {
            de.keksuccino.fancymenu.customization.background.MenuBackgroundRegistry
                    .register(new top.yunmouren.craftbrowser.fancymenu.BrowserMenuBackgroundBuilder("NCEF"));
        }

    }
}