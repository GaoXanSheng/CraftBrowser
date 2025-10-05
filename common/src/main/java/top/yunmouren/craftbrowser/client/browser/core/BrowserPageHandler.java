package top.yunmouren.craftbrowser.client.browser.core;

import io.webfolder.cdp.session.Session;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.Map;

public record BrowserPageHandler(Session session) {

    public void loadUrl(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        String finalUrl = url;
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "http://" + finalUrl;
        }
        session.send("Page.navigate", Map.of("url", finalUrl));
    }

    public void customizeLoadingScreenUrl() {
        this.loadCustomizeURL(Config.CLIENT.customizeLoadingScreenUrl.get());
    }

    public void loadCustomizeURL(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        session.send("Page.navigate", Map.of("url", url));
    }
}