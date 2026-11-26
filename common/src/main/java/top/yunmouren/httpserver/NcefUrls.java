package top.yunmouren.httpserver;

public final class NcefUrls {
    public static final String SCHEME = "ncef://";

    private NcefUrls() {
    }

    public static String resolve(String url) {
        if (url == null || !url.startsWith(SCHEME)) return url;

        String path = url.substring(SCHEME.length());
        int query = path.indexOf('?');
        String suffix = "";
        if (query >= 0) {
            suffix = path.substring(query);
            path = path.substring(0, query);
        }
        if (path.isEmpty()) path = "index.html";

        int port = ServerHttp.ensureStarted();
        if (port <= 0) return url;

        return "http://127.0.0.1:" + port + "/" + path + suffix;
    }
}
