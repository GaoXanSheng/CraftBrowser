package top.yunmouren.craftbrowser.client.browser.Controller;

public interface IMasterController {
    // See NCEF IMasterController
    String CreateBrowser(String url, int w, int h, int fps);

    void StopBrowser(String spoutId);

    void Shutdown();
}