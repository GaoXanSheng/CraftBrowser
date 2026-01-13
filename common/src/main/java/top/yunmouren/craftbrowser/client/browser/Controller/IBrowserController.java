package top.yunmouren.craftbrowser.client.browser.Controller;

public interface IBrowserController {
    // See NCEF IBrowserController
    String GetSpoutId();

    void LoadUrl(String url);

    void ExecuteJs(String script);

    String GetUrl();

    void SetAudioMuted(Boolean b);

    void SetVolume(float vol);

    Boolean Resize(int width, int height, int deviceScaleFactor, Boolean mobile);

    void SendMouseMove(int x, int y, Boolean mouseLeave);

    void SendMouseMove(int x, int y);

    void SendMouseClick(int x, int y, int button, Boolean mouseUp);

    void SendMouseWheel(int x, int y, int deltaX, int deltaY);

    void SendKeyEvent(int windowsKeyCode, Boolean isUp);

    void SendText(String text);

    int GetCursorType();

    void ResolveJsPromise(String reqId, Object result);
}