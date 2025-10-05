package top.yunmouren.craftbrowser.client.browser.core;

import com.hubspot.chrome.devtools.base.ChromeRequest;
import com.hubspot.chrome.devtools.client.ChromeDevToolsClient;
import com.hubspot.chrome.devtools.client.ChromeDevToolsSession;
import com.hubspot.chrome.devtools.client.core.target.TargetID;
import com.hubspot.chrome.devtools.client.core.target.TargetInfo;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class BrowserLifecycleManager {
    private ChromeDevToolsSession session;

    public BrowserLifecycleManager(String host, int port) {
        init(host, port);
    }

    private void init(String host, int port) {
        ChromeDevToolsClient client = ChromeDevToolsClient.defaultClient();
        try {
            this.session = client.connect(host, port);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ChromeDevToolsSession getSession() {
        return session;
    }

    public void resizeViewport(int width, int height) {
        if (session == null) return;
        ChromeRequest request = new ChromeRequest("Emulation.setDeviceMetricsOverride")
                .putParams("width", width)
                .putParams("height", height)
                .putParams("deviceScaleFactor", 1.0)
                .putParams("mobile", false);
        session.send(request);
    }

    public void onClose() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception e) {
            Craftbrowser.LOGGER.warn("Error closing session", e);
        }
    }
}