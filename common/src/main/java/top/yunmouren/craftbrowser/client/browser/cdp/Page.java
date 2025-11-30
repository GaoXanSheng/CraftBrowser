package top.yunmouren.craftbrowser.client.browser.cdp;

import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public record Page(Session session) {

    public void navigate(String url) {
        if (session == null || url == null || url.isEmpty()) return;

        JsonObject params = new JsonObject();
        params.addProperty("url", url);

        session.send("Page.navigate", params);
    }

    public void enable() {
        if (session == null) return;
        session.send("Page.enable", null);
    }

    public void goBack() {
        if (session == null) return;

        // 获取导航历史
        getNavigationHistory().thenAccept(history -> {
            if (history.has("currentIndex") && history.has("entries")) {
                int currentIndex = history.get("currentIndex").getAsInt();
                if (currentIndex > 0) {
                    var entries = history.getAsJsonArray("entries");
                    int targetIndex = currentIndex - 1;
                    var targetEntry = entries.get(targetIndex).getAsJsonObject();
                    int entryId = targetEntry.get("id").getAsInt();

                    JsonObject params = new JsonObject();
                    params.addProperty("entryId", entryId);
                    session.send("Page.navigateToHistoryEntry", params);
                }
            }
        });
    }

    public void goForward() {
        if (session == null) return;

        getNavigationHistory().thenAccept(history -> {
            if (history.has("currentIndex") && history.has("entries")) {
                int currentIndex = history.get("currentIndex").getAsInt();
                var entries = history.getAsJsonArray("entries");
                if (currentIndex < entries.size() - 1) {
                    int targetIndex = currentIndex + 1;
                    var targetEntry = entries.get(targetIndex).getAsJsonObject();
                    int entryId = targetEntry.get("id").getAsInt();

                    JsonObject params = new JsonObject();
                    params.addProperty("entryId", entryId);
                    session.send("Page.navigateToHistoryEntry", params);
                }
            }
        });
    }

    public void reload() {
        if (session == null) return;
        session.send("Page.reload", null);
    }

    public void hardReload() {
        if (session == null) return;

        JsonObject params = new JsonObject();
        params.addProperty("ignoreCache", true);
        session.send("Page.reload", params);
    }

    public void stopLoading() {
        if (session == null) return;
        session.send("Page.stopLoading", null);
    }

    public CompletableFuture<JsonObject> getNavigationHistory() {
        if (session == null) {
            return CompletableFuture.completedFuture(new JsonObject());
        }
        return session.send("Page.getNavigationHistory", null);
    }

    public void onLoadEventFired(Runnable handler) {
        if (session == null) return;
        session.on("Page.loadEventFired", params -> handler.run());
    }

    public void onDomContentEventFired(Runnable handler) {
        if (session == null) return;
        session.on("Page.domContentEventFired", params -> handler.run());
    }

    public void onFrameNavigated(Consumer<String> urlHandler) {
        if (session == null) return;
        session.on("Page.frameNavigated", params -> {
            if (params != null && params.has("frame")) {
                var frame = params.getAsJsonObject("frame");
                if (frame.has("url")) {
                    String url = frame.get("url").getAsString();
                    urlHandler.accept(url);
                }
            }
        });
    }
}