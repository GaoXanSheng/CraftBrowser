package top.yunmouren.cdp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Session implements WebSocket.Listener {
    private final WebSocket webSocket;
    private final Gson gson = new Gson();
    private final AtomicLong messageId = new AtomicLong(1);
    private final ConcurrentHashMap<Long, CompletableFuture<JsonObject>> pendingCommands = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Consumer<JsonObject>> eventHandlers = new ConcurrentHashMap<>();

    // 通过静态工厂方法创建实例
    public static CompletableFuture<Session> connect(String webSocketUrl) {
        CompletableFuture<Session> sessionFuture = new CompletableFuture<>();
        HttpClient client = HttpClient.newHttpClient();
        // 传入一个 sessionFuture，这样 Listener 可以在连接成功后完成它
        client.newWebSocketBuilder()
                .buildAsync(URI.create(webSocketUrl), new Session.WebSocketListener(sessionFuture));
        return sessionFuture;
    }

    // 私有构造函数，防止直接实例化
    private Session(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public CompletableFuture<JsonObject> send(String method, JsonObject params) {
        long id = messageId.getAndIncrement();
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        pendingCommands.put(id, future);

        JsonObject command = new JsonObject();
        command.addProperty("id", id);
        command.addProperty("method", method);
        if (params != null) {
            command.add("params", params);
        }

        webSocket.sendText(gson.toJson(command), true);
        return future;
    }

    public void on(String eventName, Consumer<JsonObject> handler) {
        eventHandlers.put(eventName, handler);
    }

    public void close() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client closing").join();
    }

    // --- WebSocket.Listener 接口实现 ---
    private static class WebSocketListener implements WebSocket.Listener {
        private final CompletableFuture<Session> sessionFuture;
        private Session session;
        private final StringBuilder textBuilder = new StringBuilder();

        WebSocketListener(CompletableFuture<Session> sessionFuture) {
            this.sessionFuture = sessionFuture;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            // 连接成功，创建 CraftSession 实例并完成 Future
            this.session = new Session(webSocket);
            sessionFuture.complete(this.session);
            // 将 WebSocket 的监听器设置为 session 自身，以便处理后续消息
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuilder.append(data);
            webSocket.request(1);

            if (last) {
                String message = textBuilder.toString();
                textBuilder.setLength(0);
                JsonObject jsonMessage = this.session.gson.fromJson(message, JsonObject.class);

                if (jsonMessage.has("id")) { // 这是一个响应
                    long id = jsonMessage.get("id").getAsLong();
                    CompletableFuture<JsonObject> future = this.session.pendingCommands.remove(id);
                    if (future != null) {
                        if (jsonMessage.has("result")) {
                            future.complete(jsonMessage.getAsJsonObject("result"));
                        } else if (jsonMessage.has("error")) {
                            future.completeExceptionally(new RuntimeException(jsonMessage.getAsJsonObject("error").toString()));
                        }
                    }
                } else if (jsonMessage.has("method")) { // 这是一个事件
                    String method = jsonMessage.get("method").getAsString();
                    Consumer<JsonObject> handler = this.session.eventHandlers.get(method);
                    if (handler != null) {
                        handler.accept(jsonMessage.getAsJsonObject("params"));
                    }
                }
            }
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.err.println("WebSocket Error: " + error.getMessage());
            if (!sessionFuture.isDone()) {
                sessionFuture.completeExceptionally(error);
            }
        }
    }
}