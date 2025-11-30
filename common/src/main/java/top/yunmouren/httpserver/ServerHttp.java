package top.yunmouren.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import top.yunmouren.craftbrowser.client.config.Config;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class ServerHttp {
    private static int boundPort = Config.CLIENT.externalHttpServerPort.get();
    public static int getBoundPort() {
        return boundPort;
    }

    private static final String WEB_ROOT = "/dist";

    public static void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(boundPort), 0);
            boundPort = server.getAddress().getPort();
            // /send 接口
            server.createContext("/api", ServerHttp::handleSend);

            // 静态资源
            server.createContext("/", ServerHttp::handleStatic);

            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // /send POST 接口
    private static void handleSend(HttpExchange exchange) {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            java.util.concurrent.CompletableFuture<String> future = new java.util.concurrent.CompletableFuture<>();


            HttpNetworkHandler.sendToServer(requestBody, future);

            String result;
            try {
                result = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                result = "{\"error\":\"timeout\"}";
            }

            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {}
        }
    }


    // 静态资源处理
    private static void handleStatic(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html"; // 默认首页

            InputStream resource = ServerHttp.class.getResourceAsStream(WEB_ROOT + path);
            if (resource == null) {
                resource = ServerHttp.class.getResourceAsStream("/dist/index.html");
                if (resource == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
            }

            byte[] bytes = resource.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", guessContentType(path));
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
            resource.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (Exception ignored) {}
        }
    }


    // 简单 MIME 类型判断
    private static String guessContentType(String path) {
        path = path.toLowerCase();

        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".xml")) return "application/xml";
        if (path.endsWith(".txt")) return "text/plain";
        if (path.endsWith(".csv")) return "text/csv";
        if (path.endsWith(".md")) return "text/markdown";

        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".bmp")) return "image/bmp";
        if (path.endsWith(".webp")) return "image/webp";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".svg")) return "image/svg+xml";

        if (path.endsWith(".mp4")) return "video/mp4";
        if (path.endsWith(".webm")) return "video/webm";
        if (path.endsWith(".ogg")) return "video/ogg";

        if (path.endsWith(".mp3")) return "audio/mpeg";
        if (path.endsWith(".wav")) return "audio/wav";
        if (path.endsWith(".ogg")) return "audio/ogg";

        if (path.endsWith(".pdf")) return "application/pdf";
        if (path.endsWith(".zip")) return "application/zip";
        if (path.endsWith(".tar")) return "application/x-tar";
        if (path.endsWith(".gz")) return "application/gzip";
        if (path.endsWith(".rar")) return "application/vnd.rar";
        if (path.endsWith(".7z")) return "application/x-7z-compressed";

        return "application/octet-stream";
    }

}
