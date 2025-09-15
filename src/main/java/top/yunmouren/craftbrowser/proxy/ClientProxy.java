package top.yunmouren.craftbrowser.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import top.yunmouren.craftbrowser.client.BrowserProcess;
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.craftbrowser.server.Server;

import static top.yunmouren.craftbrowser.Craftbrowser.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IProxy {
    @Override
    public void init() {
        new Server();
    }
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ConfigListener {
        @SubscribeEvent
        public static void onLoadConfig(ModConfigEvent.Loading event) {
            if (event.getConfig().getSpec() == Config.CLIENT_SPEC) {
                new BrowserProcess();
            }
        }
    }
}