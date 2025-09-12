package top.yunmouren.craftbrowser.client.browser;

import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WebScreen extends AbstractWebScreen {

    public WebScreen(String url) {
        super(new TextComponent("WebScreen"));
        browserManager.loadUrl(url);
    }
    public WebScreen() {
        super(new TextComponent("WebScreen"));
        browserManager.loadUrl("https://example.com/");
    }
}
