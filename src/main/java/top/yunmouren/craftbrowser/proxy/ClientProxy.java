package top.yunmouren.craftbrowser.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.yunmouren.craftbrowser.client.browser.BrowserProcess;
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.craftbrowser.server.Server;

@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IProxy {
    @Override
    public void init() {
        new Config.Client().load();
        new Server();
        new BrowserProcess();
    }
}