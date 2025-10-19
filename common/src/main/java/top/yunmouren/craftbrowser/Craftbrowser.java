package top.yunmouren.craftbrowser;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.slf4j.Logger;
import top.yunmouren.craftbrowser.client.browser.core.BrowserInstance;
import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.httpserver.ServerHttp;


public final class Craftbrowser {
    public static final String MOD_ID = "craftbrowser";;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        Config.CLIENT.load();
        if (Platform.getEnvironment() == Env.CLIENT) {
            if (Config.CLIENT.externalHttpServer.get()) {
                ServerHttp.startServer();
            }
            new BrowserInstance();
        }
    }
}
