package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;

public class BrowserAPI {

    private static BrowserAPI INSTANCE;
    private final BrowserManager manager = new BrowserManager();
    private final BrowserRender render= new BrowserRender();

    public static BrowserAPI getInstance() {
        if (INSTANCE==null){
            BrowserAPI.INSTANCE = new BrowserAPI();
        }
        return INSTANCE;
    }

    public BrowserRender getRender() {
        return render;
    }

    public BrowserManager getManager() {
        return manager;
    }
}
