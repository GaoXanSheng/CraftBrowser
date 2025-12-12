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
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

public class BrowserFactory {
    private final Session session;
    private final Page defaultPage;
    private final Input defaultInput;
    private final Emulation defaultEmulation;
    private final Runtime defaultRuntime;

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Browser-Discovery-Worker");
        t.setDaemon(true);
        return t;
    });

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private BrowserFactory(Session session) {
        this.session = session;
        this.defaultPage = new Page(this.session);
        this.defaultInput = new Input(this.session);
        this.defaultEmulation = new Emulation(this.session);
        this.defaultRuntime = new Runtime(this.session);
    }
    public static CompletableFuture<BrowserFactory> launchAsync(String host, int port, String id) {
        CompletableFuture<BrowserFactory> future = new CompletableFuture<>();
        attemptConnection(host, port, id, 0, 500, future);
        return future;
    }

    private static void attemptConnection(String host, int port, String id, int currentRetry, int maxRetries, CompletableFuture<BrowserFactory> future) {
        if (currentRetry >= maxRetries) {
            future.completeExceptionally(new BrowserLaunchException("Timeout: Failed to connect to browser page '" + id + "' after " + maxRetries + " attempts.", null));
            return;
        }
        try {
            List<Target> availableTargets = fetchAvailableTargets(host, port);
            String webSocketUrl = findExactMatchingPageUrl(availableTargets, id);

            if (webSocketUrl != null) {
                Session.connect(webSocketUrl).handle((session, ex) -> {
                    if (ex != null) {
                        scheduleRetry(host, port, id, currentRetry, maxRetries, future);
                    } else {
                        future.complete(new BrowserFactory(session));
                    }
                    return null;
                });
            } else {
                scheduleRetry(host, port, id, currentRetry, maxRetries, future);
            }
        } catch (Exception e) {
            scheduleRetry(host, port, id, currentRetry, maxRetries, future);
        }
    }

    private static void scheduleRetry(String host, int port, String id, int currentRetry, int maxRetries, CompletableFuture<BrowserFactory> future) {
        SCHEDULER.schedule(() -> {
            attemptConnection(host, port, id, currentRetry + 1, maxRetries, future);
        }, 100, TimeUnit.MILLISECONDS);
    }
    @Deprecated
    public static BrowserFactory launch(String host, int port, String id) {
        try {
            return launchAsync(host, port, id).get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new BrowserLaunchException("Sync launch failed", e);
        }
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

    private static List<Target> fetchAvailableTargets(String host, int port) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%d/json/list", host, port)))
                .GET()
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP error code: " + response.statusCode());
        }

        Type listType = new TypeToken<List<Target>>() {}.getType();
        return new Gson().fromJson(response.body(), listType);
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