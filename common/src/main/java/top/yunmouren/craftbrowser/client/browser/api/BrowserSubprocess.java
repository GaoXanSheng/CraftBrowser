package top.yunmouren.craftbrowser.client.browser.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunmouren.craftbrowser.client.browser.cdp.BrowserFactory;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserKeyHandler;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserMouseHandler;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserPageHandler;
import top.yunmouren.craftbrowser.client.browser.util.JSScript;
import top.yunmouren.craftbrowser.client.config.Config;

public class BrowserSubprocess {
    private static final Logger log = LoggerFactory.getLogger(BrowserSubprocess.class);
    private volatile BrowserRender render;

    private final BrowserMouseHandler mouseHandler;
    private final BrowserKeyHandler keyHandler;
    private final BrowserPageHandler pageHandler;
    private final BrowserFactory browserFactory;
    private final String spoutID;

    BrowserSubprocess(String spoutID) {
        String host = "127.0.0.1";
        int port = Config.CLIENT.customizeBrowserPort.get();
        this.spoutID = spoutID;
        this.browserFactory = BrowserFactory.launch(host, port, spoutID);
        this.mouseHandler = new BrowserMouseHandler(this.browserFactory);
        this.keyHandler = new BrowserKeyHandler(this.browserFactory);
        this.pageHandler = new BrowserPageHandler(this.browserFactory);
        initializeCursorListener(browserFactory);
    }

    private void initializeCursorListener(BrowserFactory browserFactory) {
        if (browserFactory == null) return;
        browserFactory.runtime().enable();
    }

    public int getRender(int width, int height) {
        if (render == null) {
            this.render = new BrowserRender(spoutID);
        }
        return render.render(width, height);
    }

    public BrowserMouseHandler getMouseHandler() {
        return mouseHandler;
    }

    public BrowserKeyHandler getKeyHandler() {
        return keyHandler;
    }

    public BrowserPageHandler getPageHandler() {
        return pageHandler;
    }

    public BrowserFactory getBrowserFactory() {
        return browserFactory;
    }
    public void SetBrowserVolume(double volume) {
        browserFactory.runtime().evaluate(JSScript.SetVolume(volume));
    }
    public void releaseSpout() {
        if (render != null) {
            render.close();
            render = null; // 防止重复释放
        }
    }
}