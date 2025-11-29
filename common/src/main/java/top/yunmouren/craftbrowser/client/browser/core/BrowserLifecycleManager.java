package top.yunmouren.craftbrowser.client.browser.core;

import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.browser.cdp.Browser;

public class BrowserLifecycleManager {
    private Browser browser;

    public BrowserLifecycleManager(String host, int port) {
        try {
            this.browser = Browser.launch(host, port);
        } catch (Exception e) {
            Craftbrowser.LOGGER.error("Failed to connect to ChromeDevTools", e);
        }
    }
    public BrowserLifecycleManager(Browser browser) {
        this.browser = browser;
    }
    public Browser getBrowser() {
        return browser;
    }

    public void resizeViewport(int width, int height) {
        if (browser == null) return;
        browser.emulation().setDeviceMetricsOverride(width, height, 1.0, false);
    }

    public void onClose() {
        try {
            if (browser != null) browser.close();
        } catch (Exception e) {
            Craftbrowser.LOGGER.warn("Error closing session", e);
        }
    }
}
