package top.yunmouren.craftcdp;

import com.google.gson.JsonObject;

public record Page(Session session) {

    public void navigate(String url) {
        if (session == null || url == null || url.isEmpty()) return;

        JsonObject params = new JsonObject();
        params.addProperty("url", url);

        session.send("Page.navigate", params);
    }
}