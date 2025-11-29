package top.yunmouren.craftbrowser.client.browser.cdp;

import com.google.gson.JsonObject;

public record Emulation(Session session) {

    public void setDeviceMetricsOverride(int width, int height, double deviceScaleFactor, boolean mobile) {
        if (session == null) return;

        JsonObject params = new JsonObject();
        params.addProperty("width", width);
        params.addProperty("height", height);
        params.addProperty("deviceScaleFactor", deviceScaleFactor);
        params.addProperty("mobile", mobile);

        session.send("Emulation.setDeviceMetricsOverride", params);
    }
}