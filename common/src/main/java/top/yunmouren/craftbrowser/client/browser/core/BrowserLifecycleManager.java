package top.yunmouren.craftbrowser.client.browser.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.chrome.devtools.base.ChromeRequest;
import com.hubspot.chrome.devtools.client.ChromeDevToolsClient;
import com.hubspot.chrome.devtools.client.ChromeDevToolsClientDefaults;
import com.hubspot.chrome.devtools.client.ChromeDevToolsSession;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.util.concurrent.CompletableFuture;

public class BrowserLifecycleManager {
    private ChromeDevToolsSession session;

    public BrowserLifecycleManager() {
        // 构造函数只做轻量初始化
    }

    public void initAsync(String host, int port) {
        ObjectMapper mapper = ChromeDevToolsClientDefaults.DEFAULT_OBJECT_MAPPER;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CompletableFuture.runAsync(() -> {
            try {
                ChromeDevToolsClient client = new ChromeDevToolsClient.Builder()
                        .setExecutorService(ChromeDevToolsClientDefaults.DEFAULT_EXECUTOR_SERVICE)
                        .setObjectMapper(mapper)
                        .setHttpClient(ChromeDevToolsClientDefaults.DEFAULT_HTTP_CLIENT)
                        .setActionTimeoutMillis(ChromeDevToolsClientDefaults.DEFAULT_CHROME_ACTION_TIMEOUT_MILLIS)
                        .setSessionConnectTimeoutMillis(ChromeDevToolsClientDefaults.DEFAULT_HTTP_CONNECTION_RETRY_TIMEOUT_MILLIS)
                        .build();
                this.session = client.connect(host, port);
            } catch (Exception e) {
                Craftbrowser.LOGGER.error("Failed to connect to ChromeDevTools", e);
            }
        });
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
            if (session != null) session.close();
        } catch (Exception e) {
            Craftbrowser.LOGGER.warn("Error closing session", e);
        }
    }
}
