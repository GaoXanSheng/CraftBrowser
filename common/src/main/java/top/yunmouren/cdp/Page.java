package top.yunmouren.cdp;

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

    /**
     * 启用页面域事件
     */
    public void enable() {
        if (session == null) return;
        session.send("Page.enable", null);
    }

    /**
     * 后退到上一页
     */
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

    /**
     * 前进到下一页
     */
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

    /**
     * 刷新当前页面
     */
    public void reload() {
        if (session == null) return;
        session.send("Page.reload", null);
    }

    /**
     * 强制刷新（忽略缓存）
     */
    public void hardReload() {
        if (session == null) return;

        JsonObject params = new JsonObject();
        params.addProperty("ignoreCache", true);
        session.send("Page.reload", params);
    }

    /**
     * 停止加载
     */
    public void stopLoading() {
        if (session == null) return;
        session.send("Page.stopLoading", null);
    }

    /**
     * 获取导航历史
     */
    public CompletableFuture<JsonObject> getNavigationHistory() {
        if (session == null) {
            return CompletableFuture.completedFuture(new JsonObject());
        }
        return session.send("Page.getNavigationHistory", null);
    }

    /**
     * 监听页面加载事件
     */
    public void onLoadEventFired(Runnable handler) {
        if (session == null) return;
        session.on("Page.loadEventFired", params -> handler.run());
    }

    /**
     * 监听 DOM 内容加载完成事件
     */
    public void onDomContentEventFired(Runnable handler) {
        if (session == null) return;
        session.on("Page.domContentEventFired", params -> handler.run());
    }

    /**
     * 监听帧导航事件
     */
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