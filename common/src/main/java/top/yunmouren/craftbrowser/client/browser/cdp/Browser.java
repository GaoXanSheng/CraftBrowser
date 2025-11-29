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

public class Browser {
    private final Session session;
    private final Page defaultPage;
    private final Input defaultInput;
    private final Emulation defaultEmulation;
    private final Runtime defaultRuntime;

    private Browser(Session session) {
        this.session = session;
        this.defaultPage = new Page(this.session);
        this.defaultInput = new Input(this.session);
        this.defaultEmulation = new Emulation(this.session);
        this.defaultRuntime = new Runtime(this.session);
    }

    // --- Public API for Launching ---

    /**
     * Connects to the first available page in the browser.
     */
    public static Browser launch(String host, int port) {
        return launch(host, port, target -> true);
    }

    /**
     * Connects to a page whose URL contains the specified string.
     */
    public static Browser launch(String host, int port, String urlPart) {
        return launch(host, port, target -> {
            if (target.has("url")) {
                return target.get("url").getAsString().contains(urlPart);
            }
            return false;
        });
    }

    // --- Core Launch Logic ---

    private static Browser launch(String host, int port, Predicate<JsonObject> pageFilter) {
        try {
            JsonArray availableTargets = fetchAvailableTargets(host, port);
            String webSocketUrl = findFirstMatchingPageUrl(availableTargets, pageFilter);
            Session session = Session.connect(webSocketUrl).join();
            return new Browser(session);
        } catch (Exception e) {
            if (e instanceof BrowserLaunchException) {
                throw (BrowserLaunchException) e;
            }
            throw new BrowserLaunchException("Failed to launch and connect to browser page", e);
        }
    }

    // --- Helper Methods for Launching ---

    /**
     * Fetches the list of debuggable targets from the browser's remote debugging endpoint.
     *
     * @return A JsonArray of targets.
     * @throws BrowserLaunchException if the HTTP request fails or the response is invalid.
     */
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

    /**
     * Finds the WebSocket URL for the first page that matches the given filter.
     * If no page matches, it falls back to the last available page found.
     *
     * @return The webSocketDebuggerUrl of the matching or fallback page.
     * @throws NoPageFoundException if no pages are available at all.
     */
    private static String findFirstMatchingPageUrl(JsonArray targets, Predicate<JsonObject> filter) {
        String lastPageUrl = null;

        for (JsonElement targetElement : targets) {
            JsonObject target = targetElement.getAsJsonObject();
            if ("page".equals(target.get("type").getAsString())) {
                // Keep track of the last seen page as a fallback.
                lastPageUrl = target.get("webSocketDebuggerUrl").getAsString();
                // If the filter matches, we've found our ideal target.
                if (filter.test(target)) {
                    Craftbrowser.LOGGER.debug("Found matching page: {}", target.get("url").getAsString());
                    return lastPageUrl;
                }
            }
        }

        // If we finished the loop without a match, use the fallback if it exists.
        if (lastPageUrl != null) {
            Craftbrowser.LOGGER.warn("No page found matching criteria. Falling back to the last available page.");
            return lastPageUrl;
        }

        // If there were no pages at all, throw the exception.
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

    // --- Custom Exceptions ---

    public static class BrowserLaunchException extends RuntimeException {
        public BrowserLaunchException(String message) {
            super(message);
        }

        public BrowserLaunchException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class NoPageFoundException extends BrowserLaunchException {
        public NoPageFoundException(String message) {
            super(message);
        }
    }
}
