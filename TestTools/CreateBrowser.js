CefSharp.BindObjectAsync("AppController").then(() => {
    AppController.createBrowser(
        "https://example.com",   // url
        1024,                    // width
        768,                     // height
        "MySpoutID",             // spoutId
        60                       // maxFps
    );
});

CefSharp.BindObjectAsync("AppController").then(() => {
    AppController.closeBrowser(
        "MySpoutID",             // spoutId
    );
});