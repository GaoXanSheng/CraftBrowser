package top.yunmouren.cdp;

import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public record Runtime(Session session) {

    /**
     * 启用 Runtime 域
     */
    public void enable() {
        if (session == null) return;
        session.send("Runtime.enable", null);
    }

    /**
     * 执行 JavaScript 表达式
     * @param expression JavaScript 表达式
     * @return 返回执行结果的 Future
     */
    public CompletableFuture<JsonObject> evaluate(String expression) {
        if (session == null) {
            return CompletableFuture.completedFuture(new JsonObject());
        }

        JsonObject params = new JsonObject();
        params.addProperty("expression", expression);
        params.addProperty("returnByValue", true);

        return session.send("Runtime.evaluate", params);
    }

    /**
     * 获取鼠标位置下元素的光标样式
     * @param x 鼠标 X 坐标
     * @param y 鼠标 Y 坐标
     * @return 返回光标样式字符串的 Future
     */
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
            } catch (Exception e) {
                // 忽略错误，返回默认值
            }
            return "default";
        });
    }
}

