package top.yunmouren.craftbrowser.client.browser.cdp;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import top.yunmouren.craftbrowser.client.browser.cdp.models.Target;
import top.yunmouren.craftbrowser.client.config.Config;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BrowserFactory {
    private final Session session;
    private final Page defaultPage;
    private final Input defaultInput;
    private final Emulation defaultEmulation;
    private final Runtime defaultRuntime;

    private BrowserFactory(Session session) {
        this.session = session;
        this.defaultPage = new Page(this.session);
        this.defaultInput = new Input(this.session);
        this.defaultEmulation = new Emulation(this.session);
        this.defaultRuntime = new Runtime(this.session);
    }

    public static BrowserFactory launch(String host, int port, String id) {
        int maxRetries = 500;
        long sleepMs = 100;

        Exception lastException = null;

        for (int i = 0; i < maxRetries; i++) {
            try {
                List<Target> availableTargets = fetchAvailableTargets(host, port);
                // 使用严格匹配逻辑
                String webSocketUrl = findExactMatchingPageUrl(availableTargets, id);

                if (webSocketUrl != null) {
                    Session session = Session.connect(webSocketUrl).join();
                    return new BrowserFactory(session);
                }
            } catch (Exception e) {
                lastException = e;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BrowserLaunchException("Interrupted while waiting for browser page: " + id, e);
            }
        }

        throw new BrowserLaunchException("Failed to find specific browser page with ID: " + id + " after retries.", lastException);
    }

    public static BrowserFactory launch(String host, int port) {
        try {
            List<Target> availableTargets = fetchAvailableTargets(host, port);
            String webSocketUrl = findFirstMatchingPageUrl(availableTargets, Config.CLIENT.customizeSpoutID.get());
            Session session = Session.connect(webSocketUrl).join();
            return new BrowserFactory(session);
        } catch (Exception e) {
            throw new BrowserLaunchException("Failed to launch default browser page", e);
        }
    }

    private static List<Target> fetchAvailableTargets(String host, int port) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://%s:%d/json/list", host, port)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Type listType = new TypeToken<List<Target>>() {}.getType();
            return new Gson().fromJson(response.body(), listType);
        } catch (Exception e) {
            throw new BrowserLaunchException("Failed to fetch debuggable targets from " + host + ":" + port, e);
        }
    }
    private static String findExactMatchingPageUrl(List<Target> targets, String id) {
        if (targets == null) return null;
        for (Target target : targets) {
            if (id.equals(target.getTitle()) && target.getWebSocketDebuggerUrl() != null) {
                return target.getWebSocketDebuggerUrl();
            }
        }
        return null;
    }

    private static String findFirstMatchingPageUrl(List<Target> targets, String id) {
        String lastPageUrl = null;
        for (Target target : targets) {
            if ("page".equals(target.getType())) {
                lastPageUrl = target.getWebSocketDebuggerUrl();
                if (id != null && id.equals(target.getTitle())) {
                    return lastPageUrl;
                }
            }
        }
        return lastPageUrl;
    }

    public Session getSession() { return session; }
    public Page page() { return this.defaultPage; }
    public Input input() { return this.defaultInput; }
    public Emulation emulation() { return this.defaultEmulation; }
    public Runtime runtime() { return this.defaultRuntime; }
    public void close() { if (session != null) session.close(); }

    public static class BrowserLaunchException extends RuntimeException {
        public BrowserLaunchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}