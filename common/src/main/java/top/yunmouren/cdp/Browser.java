package top.yunmouren.cdp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

    public static Browser launch(String host, int port) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("http://%s:%d/json/list", host, port)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Gson gson = new Gson();
            JsonArray targets = gson.fromJson(response.body(), JsonArray.class);

            String pageWebSocketUrl = null;
            for (JsonElement targetElement : targets) {
                JsonObject target = targetElement.getAsJsonObject();
                if (target.get("type").getAsString().equals("page")) {
                    pageWebSocketUrl = target.get("webSocketDebuggerUrl").getAsString();
                    break;
                }
            }

            if (pageWebSocketUrl == null) {
                throw new RuntimeException("No debuggable page found. Make sure Chrome has at least one tab open.");
            }

            return Session.connect(pageWebSocketUrl)
                    .thenApply(Browser::new)
                    .join();
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch and connect to browser page", e);
        }
    }

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
}