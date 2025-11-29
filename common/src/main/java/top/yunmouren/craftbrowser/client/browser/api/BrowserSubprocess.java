package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.cdp.Browser;
import top.yunmouren.craftbrowser.client.browser.core.*;
import top.yunmouren.craftbrowser.client.config.Config;

public class BrowserSubprocess {
    private final BrowserRender render;
    public final BrowserLifecycleManager lifecycleManager;
    public final BrowserMouseHandler mouseHandler;
    public final BrowserKeyHandler keyHandler;
    public final BrowserPageHandler pageHandler;

    public BrowserSubprocess(String url, int width, int height, String spoutID, int maxFps) {
        BrowserAPI.getInstance().getManager().getBrowser().runtime().evaluate(getScript(
                url, width, height, spoutID, maxFps
        ));
        String host = "127.0.0.1";
        int port = Config.CLIENT.customizeBrowserPort.get();
        var browser = Browser.launch(host, port, url);
        lifecycleManager = new BrowserLifecycleManager(browser);
        mouseHandler = new BrowserMouseHandler(browser);
        keyHandler = new BrowserKeyHandler(browser);
        pageHandler = new BrowserPageHandler(browser);
        render = new BrowserRender(spoutID, width, height);
    }

    private String getScript(String url, int width, int height, String spoutID, int maxFps) {
        return """
                CefSharp.BindObjectAsync("appController").then(() => {
                    appController.createBrowser(
                        "%s",
                        %d,
                        %d,
                        "%s",
                        %d
                    );
                });
                """.formatted(url, width, height, spoutID, maxFps);
    }

    public BrowserRender getRender() {
        return render;
    }

}
