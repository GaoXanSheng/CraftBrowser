package top.yunmouren.craftbrowser.client.browser.cdp.models;

public class Target {
    private String id;
    private String type;
    private String title;
    private String url;
    private String webSocketDebuggerUrl;

    // Getter 方法
    public String getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getWebSocketDebuggerUrl() { return webSocketDebuggerUrl; }

    @Override
    public String toString() {
        return "Target{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", webSocketDebuggerUrl='" + webSocketDebuggerUrl + '\'' +
                '}';
    }
}
