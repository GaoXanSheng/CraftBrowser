package top.yunmouren.craftbrowser.client.browser.core;

import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import io.webfolder.cdp.session.SessionInfo;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.util.List;
import java.util.Map;

public class BrowserLifecycleManager {
    private SessionFactory factory;
    private List<SessionInfo> pages;
    private Session session;

    public BrowserLifecycleManager(String host, int port) {
        init(host, port);
    }

    private void init(String host, int port) {
        try {
            this.factory = new SessionFactory(host, port);
            pages = factory.list();
            if (!pages.isEmpty()) {
                pages.add(new SessionInfo());
            }
            this.session = factory.connect(pages.get(0).getId());
        } catch (Exception e) {
            Craftbrowser.LOGGER.error("Error connecting to Chrome DevTools: ", e);
        }
    }

    public Session getSession() {
        return session;
    }

    public void resizeViewport(int width, int height) {
        if (session == null) return;
        Map<String, Object> params = Map.of(
                "width", width,
                "height", height,
                "deviceScaleFactor", 1.0,
                "mobile", false
        );
        session.send("Emulation.setDeviceMetricsOverride", params);
    }

    public void onClose() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception e) {
            Craftbrowser.LOGGER.warn("Error closing session", e);
        }
        try {
            if (factory != null) {
                factory.close();
            }
        } catch (Exception e) {
            Craftbrowser.LOGGER.warn("Error closing factory", e);
        }
    }
}