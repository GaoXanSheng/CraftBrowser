package top.yunmouren.craftbrowser;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.slf4j.Logger;
import top.yunmouren.craftbrowser.client.browser.core.BrowserInstance;
import top.yunmouren.craftbrowser.client.config.Config;


public final class Craftbrowser {
    public static final String MOD_ID = "craftbrowser";;
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String MinecraftHttpserverPath;

    public static void init() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            Config.CLIENT.load();
            new BrowserInstance();
            try {
                Class.forName("top.yunmouren.minecrafthttpserver.Minecrafthttpserver");
                var port = top.yunmouren.minecrafthttpserver.ServerHttp.getBoundPort();
                MinecraftHttpserverPath = "http://127.0.0.1:" + port + "/";
            } catch (ClassNotFoundException e) {
                System.out.println("Minecrafthttpserver NotLoadedSkippingRegistration。");
                MinecraftHttpserverPath = "";
            }
        }
    }
}
