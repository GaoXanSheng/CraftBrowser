package top.yunmouren.craftbrowser.client.browser.api;

public interface IMasterController {
    // See NCEF IMasterController
    String CreateBrowser(String url, int w, int h, int fps);
    void StopBrowser(String spoutId);
    void Shutdown();
}