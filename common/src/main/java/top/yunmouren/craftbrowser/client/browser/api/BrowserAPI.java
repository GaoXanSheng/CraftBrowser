package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;

public class BrowserAPI {

    private static final BrowserAPI INSTANCE = new BrowserAPI();
    private final BrowserManager manager = new BrowserManager();
    private final BrowserRender render = new BrowserRender();
    private BrowserAPI() {

    }

    public static BrowserAPI getInstance() {
        return INSTANCE;
    }
    public BrowserRender getRender(){
        return render;
    }
    public BrowserManager getManager() {
        return manager;
    }
}
