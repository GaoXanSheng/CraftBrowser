package top.yunmouren.craftbrowser;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.slf4j.Logger;
import top.yunmouren.craftbrowser.client.browser.BrowserProcess;
import top.yunmouren.craftbrowser.config.Config;


public final class Craftbrowser {
    public static final String MOD_ID = "craftbrowser";;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        Config.CLIENT.load();
        if (Platform.getEnvironment() == Env.CLIENT) {
            Config.CLIENT.load();
            new BrowserProcess();
        }
    }
}
