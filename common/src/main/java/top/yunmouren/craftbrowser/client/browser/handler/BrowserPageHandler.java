package top.yunmouren.craftbrowser.client.browser.handler;

import top.yunmouren.craftbrowser.client.config.Config;
import top.yunmouren.craftbrowser.client.browser.cdp.BrowserFactory;

public record BrowserPageHandler(BrowserFactory browserFactory) {


    public void loadUrl(String url) {
        if (browserFactory == null || url == null || url.isEmpty()) return;
        String finalUrl = url;
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "http://" + finalUrl;
        }
        browserFactory.page().navigate(finalUrl);
    }

    public void customizeLoadingScreenUrl() {
        this.loadCustomizeURL(Config.CLIENT.customizeLoadingScreenUrl.get());
    }
    public void resizeViewport(int width, int height) {
        if (browserFactory == null) return;
        browserFactory.emulation().setDeviceMetricsOverride(width, height, 1.0, false);
    }
    public void loadCustomizeURL(String url) {
        if (browserFactory == null || url == null || url.isEmpty()) return;
        browserFactory.page().navigate(url);
    }
}