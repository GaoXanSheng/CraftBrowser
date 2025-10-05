package top.yunmouren.craftbrowser.client.browser.core;

import com.hubspot.chrome.devtools.client.ChromeDevToolsSession;
import top.yunmouren.craftbrowser.client.config.Config;

public record BrowserPageHandler(ChromeDevToolsSession session) {

    public void loadUrl(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        String finalUrl = url;
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "http://" + finalUrl;
        }
        session.navigate(finalUrl);
    }

    public void customizeLoadingScreenUrl() {
        this.loadCustomizeURL(Config.CLIENT.customizeLoadingScreenUrl.get());
    }

    public void loadCustomizeURL(String url) {
        if (session == null || url == null || url.isEmpty()) return;
        session.navigate(url);
    }
}