package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.core.BrowserManager;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;

public class BrowserAPI {

    private static final BrowserAPI INSTANCE;

    static {
        // 静态块中提前初始化
        INSTANCE = new BrowserAPI();
    }

    private final BrowserManager manager;
    private final BrowserRender render;

    private BrowserAPI() {
        this.manager = new BrowserManager();
        this.render = new BrowserRender();
    }

    public static BrowserAPI getInstance() {
        return INSTANCE;
    }

    public BrowserRender getRender() {
        return render;
    }

    public BrowserManager getManager() {
        return manager;
    }
}
