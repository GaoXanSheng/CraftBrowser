package top.yunmouren.craftbrowser.client.gui;

import net.minecraft.network.chat.Component;
import top.yunmouren.craftbrowser.client.browser.ui.AbstractWebScreen;
import top.yunmouren.httpserver.ServerHttp;

public class WebScreen extends AbstractWebScreen {

    public WebScreen(String url) {
        // 调用带布尔参数的构造函数，默认 false
        this(url, false);
    }

    public WebScreen(String url, boolean loadCustomizeURL) {
        super(Component.literal("WebScreen"));
        if (loadCustomizeURL) {
            browserManager.getPageHandler().loadCustomizeURL(url);
        } else {
            browserManager.getPageHandler().loadUrl(url);
        }
    }

    public WebScreen() {
        super(Component.literal("WebScreen"));
        if (ServerHttp.getBoundPort() == 0) {
            browserManager.getPageHandler().customizeLoadingScreenUrl();
        } else {
            browserManager.getPageHandler().loadUrl("http://localhost:" + ServerHttp.getBoundPort() + "/");
        }
    }
}
