package top.yunmouren.craftbrowser.client.browser.cdp;

import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public record Runtime(Session session) {
    public void enable() {
        if (session == null) return;
        session.send("Runtime.enable", null);
    }

    public CompletableFuture<JsonObject> evaluate(String expression) {
        if (session == null) {
            return CompletableFuture.completedFuture(new JsonObject());
        }

        JsonObject params = new JsonObject();
        params.addProperty("expression", expression);
        params.addProperty("returnByValue", true);
        params.addProperty("awaitPromise", true);

        return session.send("Runtime.evaluate", params);
    }

    public CompletableFuture<String> getCursorAtPosition(int x, int y) {
        String js = String.format(
            "(function() {" +
            "  var el = document.elementFromPoint(%d, %d);" +
            "  if (!el) return 'default';" +
            "  var style = window.getComputedStyle(el);" +
            "  return style.cursor || 'default';" +
            "})()",
            x, y
        );

        return evaluate(js).thenApply(result -> {
            try {
                if (result.has("result") && result.getAsJsonObject("result").has("value")) {
                    return result.getAsJsonObject("result").get("value").getAsString();
                }
            } catch (Exception ignored) {
            }
            return "default";
        });
    }
}

