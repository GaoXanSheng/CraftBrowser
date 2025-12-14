package top.yunmouren.craftbrowser.client.browser.util;

public class JSScript {
    public static String CreateBrowser(String url, int width, int height, String spoutID, int maxFps) {
        return """
                CefSharp.BindObjectAsync('AppController').then(() => {
                    AppController.createBrowser("%s", %d, %d, "%s", %d);
                });
                """.formatted(url, width, height, spoutID, maxFps);
    }

    public static String CloseBrowser(String spoutID) {
        return """
                CefSharp.BindObjectAsync("AppController").then(() => {
                    AppController.closeBrowser("%s");
                });
                """.formatted(spoutID);
    }

    public static String SetVolume(double volume) {
        volume = Math.max(0, Math.min(1, volume));
        String volStr = String.format(java.util.Locale.US, "%.2f", volume);
        return """ 
                document.querySelectorAll('video, audio').forEach(function(elem) {
                    elem.volume = %s;
                });
                """.formatted(volStr);
    }
}
