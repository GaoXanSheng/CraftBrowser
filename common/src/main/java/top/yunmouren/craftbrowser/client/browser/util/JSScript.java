package top.yunmouren.craftbrowser.client.browser.util;

public class JSScript {
    public static String getCreateScript(String url, int width, int height, String spoutID, int maxFps) {
        return """
                CefSharp.BindObjectAsync('AppController').then(() => {
                    AppController.createBrowser("%s", %d, %d, "%s", %d);
                });
                """.formatted(url, width, height, spoutID, maxFps);
    }

    public static String getRemoveScript(String spoutID) {
        return """
                CefSharp.BindObjectAsync("AppController").then(() => {
                    AppController.closeBrowser("%s");
                });
                """.formatted(spoutID);
    }

}
