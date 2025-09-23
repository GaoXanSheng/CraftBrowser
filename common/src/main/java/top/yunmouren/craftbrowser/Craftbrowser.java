package top.yunmouren.craftbrowser;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import org.slf4j.Logger;
import top.yunmouren.craftbrowser.client.browser.BrowserProcess;
import top.yunmouren.craftbrowser.client.config.Config;


public final class Craftbrowser {
    public static final String MOD_ID = "craftbrowser";;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        if (Platform.getEnvironment() == Env.CLIENT) {
            Config.CLIENT.load();
            Config.CLIENT.save();
            new BrowserProcess();
        }
    }
}
