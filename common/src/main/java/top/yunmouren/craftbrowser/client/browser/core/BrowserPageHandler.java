package top.yunmouren.craftbrowser.client.browser.core;

import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.craftcdp.Browser;

public record BrowserPageHandler(Browser session) {

    public void loadUrl(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        String finalUrl = url;
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "http://" + finalUrl;
        }
        session.page().navigate(finalUrl);
    }

    public void customizeLoadingScreenUrl() {
        this.loadCustomizeURL(Config.CLIENT.customizeLoadingScreenUrl.get());
    }

    public void loadCustomizeURL(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        session.page().navigate(url);
    }
}