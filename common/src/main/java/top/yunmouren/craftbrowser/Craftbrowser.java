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

        }
    }
    public static void onHttpserver(){
        if (Platform.isModLoaded("minecrafthttpserver")) {
            try {
                int port = top.yunmouren.minecrafthttpserver.ServerHttp.getBoundPort();
                MinecraftHttpserverPath = "http://127.0.0.1:" + port + "/";
                LOGGER.info("Minecrafthttpserver loaded, path: {}", MinecraftHttpserverPath);
            } catch (Throwable t) {
                LOGGER.warn("Minecrafthttpserver detected but failed to get port", t);
            }
        } else {
            LOGGER.info("Minecrafthttpserver not loaded, skipping registration.");
        }
    }
}
