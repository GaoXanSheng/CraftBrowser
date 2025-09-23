package top.yunmouren.craftbrowser.client.gui;

import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.yunmouren.craftbrowser.client.browser.AbstractWebScreen;

@OnlyIn(Dist.CLIENT)
public class WebScreen extends AbstractWebScreen {

    public WebScreen(String url) {
        // 调用带布尔参数的构造函数，默认 false
        this(url, false);
    }

    public WebScreen(String url, boolean loadCustomizeURL) {
        super(new TextComponent("WebScreen"));
        if (loadCustomizeURL) {
            browserManager.loadCustomizeURL(url);
        } else {
            browserManager.loadUrl(url);
        }
    }
    public WebScreen() {
        super(new TextComponent("WebScreen"));
        browserManager.loadUrl("https://example.com/");
    }
}
