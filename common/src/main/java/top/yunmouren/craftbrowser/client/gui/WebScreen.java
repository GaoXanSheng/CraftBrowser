package top.yunmouren.craftbrowser.client.gui;

import net.minecraft.network.chat.Component;
import top.yunmouren.craftbrowser.client.browser.ui.AbstractWebScreen;

public class WebScreen extends AbstractWebScreen {


    public WebScreen(String url) {
        super(Component.literal("WebScreen"),url);
    }
}
