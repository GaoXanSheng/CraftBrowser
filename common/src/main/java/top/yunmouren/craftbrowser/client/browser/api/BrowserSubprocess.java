package top.yunmouren.craftbrowser.client.browser.api;

import top.yunmouren.craftbrowser.client.browser.cdp.BrowserFactory;
import top.yunmouren.craftbrowser.client.browser.core.BrowserRender;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserKeyHandler;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserMouseHandler;
import top.yunmouren.craftbrowser.client.browser.handler.BrowserPageHandler;
import top.yunmouren.craftbrowser.client.browser.util.CursorType;
import top.yunmouren.craftbrowser.client.config.Config;

import java.util.concurrent.atomic.AtomicReference;

public class BrowserSubprocess {
    private BrowserRender render;
    private final BrowserMouseHandler mouseHandler;
    private final BrowserKeyHandler keyHandler;
    private final BrowserPageHandler pageHandler;
    private final BrowserFactory browserFactory;
    private final String spoutID;
    private final AtomicReference<CursorType> currentCursor = new AtomicReference<>(CursorType.DEFAULT);
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
    public void updateCursorAtPosition(int x, int y) {
        if (browserFactory == null) return;

        browserFactory.runtime().getCursorAtPosition(x, y).thenAccept(cursorStyle -> {
            CursorType newCursor = CursorType.fromCssValue(cursorStyle);
            currentCursor.set(newCursor);
        });
    }
    public CursorType getCurrentCursor() {
        return currentCursor.get();
    }
    public int getRender(int width, int height) {
        if (render == null) {
            this.render = new BrowserRender(spoutID, width, height);
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

    public void releaseSpout() {
        render.close();
    }
}
