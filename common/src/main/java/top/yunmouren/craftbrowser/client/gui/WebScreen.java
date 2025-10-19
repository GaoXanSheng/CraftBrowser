package top.yunmouren.craftbrowser.client.gui;

import net.minecraft.network.chat.Component;
import top.yunmouren.craftbrowser.client.browser.ui.AbstractWebScreen;
import top.yunmouren.minecrafthttpserver.ServerHttp;

public class WebScreen extends AbstractWebScreen {

    public WebScreen(String url) {
        // 调用带布尔参数的构造函数，默认 false
        this(url, false);
    }

    public WebScreen(String url, boolean loadCustomizeURL) {
        super(Component.literal("WebScreen"));
        if (loadCustomizeURL) {
            browserManager.loadCustomizeURL(url);
        } else {
            browserManager.loadUrl(url);
        }
    }

    public WebScreen() {
        super(Component.literal("WebScreen"));
        if (ServerHttp.getBoundPort() == 0) {
            browserManager.customizeLoadingScreenUrl();
        } else {
            browserManager.loadUrl("http://localhost:" + ServerHttp.getBoundPort() + "/");
        }
    }
}
