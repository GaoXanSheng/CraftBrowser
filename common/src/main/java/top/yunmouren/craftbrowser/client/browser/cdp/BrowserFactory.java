package top.yunmouren.craftbrowser.client.browser.cdp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Predicate;

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

    public static BrowserFactory launch(String host, int port) {
        return launch(host, port, target -> true);
    }

    public static BrowserFactory launch(String host, int port,String id) {
        return launch(host, port, target -> {
            JsonElement idElement = target.get("title");
            return idElement != null && idElement.getAsString().equals(id);
        });
    }

    private static BrowserFactory launch(String host, int port, Predicate<JsonObject> pageFilter) {
        try {
            JsonArray availableTargets = fetchAvailableTargets(host, port);
            String webSocketUrl = findFirstMatchingPageUrl(availableTargets, pageFilter);
            Session session = Session.connect(webSocketUrl).join();
            return new BrowserFactory(session);
        } catch (Exception e) {
            if (e instanceof BrowserLaunchException) {
                throw (BrowserLaunchException) e;
            }
            throw new BrowserLaunchException("Failed to launch and connect to browser page", e);
        }
    }

    private static JsonArray fetchAvailableTargets(String host, int port) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://%s:%d/json/list", host, port)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Gson().fromJson(response.body(), JsonArray.class);
        } catch (Exception e) {
            throw new BrowserLaunchException("Failed to fetch debuggable targets from " + host + ":" + port, e);
        }
    }

    private static String findFirstMatchingPageUrl(JsonArray targets, Predicate<JsonObject> filter) {
        String lastPageUrl = null;

        for (JsonElement targetElement : targets) {
            JsonObject target = targetElement.getAsJsonObject();
            if ("page".equals(target.get("type").getAsString())) {
                lastPageUrl = target.get("webSocketDebuggerUrl").getAsString();
                if (filter.test(target)) {
                    Craftbrowser.LOGGER.debug("Found matching page: {}", target.get("url").getAsString());
                    return lastPageUrl;
                }
            }
        }

        if (lastPageUrl != null) {
            Craftbrowser.LOGGER.warn("No page found matching criteria. Falling back to the last available page.");
            return lastPageUrl;
        }
        throw new NoPageFoundException("No debuggable page found at all.");
    }


    // --- Instance Accessors ---

    public Session getSession() {
        return session;
    }

    public Page page() {
        return this.defaultPage;
    }

    public Input input() {
        return this.defaultInput;
    }

    public Emulation emulation() {
        return this.defaultEmulation;
    }

    public Runtime runtime() {
        return this.defaultRuntime;
    }

    public void close() {
        if (session != null) {
            session.close();
        }
    }
    public static class BrowserLaunchException extends RuntimeException {
        public BrowserLaunchException(String message) { super(message); }
        public BrowserLaunchException(String message, Throwable cause) { super(message, cause); }
    }

    public static class NoPageFoundException extends BrowserLaunchException {
        public NoPageFoundException(String message) { super(message); }
    }
}
