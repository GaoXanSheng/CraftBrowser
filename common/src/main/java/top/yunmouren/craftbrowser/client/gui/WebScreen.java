package top.yunmouren.craftbrowser.client.gui;

import net.minecraft.network.chat.Component;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.browser.ui.AbstractWebScreen;

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
        if (Craftbrowser.MinecraftHttpserverPath == null || Craftbrowser.MinecraftHttpserverPath.isEmpty()) {
            browserManager.customizeLoadingScreenUrl();
        } else {
            browserManager.loadUrl(Craftbrowser.MinecraftHttpserverPath);
        }
    }
}
