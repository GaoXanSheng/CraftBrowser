CefSharp.BindObjectAsync("appController").then(() => {
    appController.createBrowser(
        "https://example.com",   // url
        1024,                    // width
        768,                     // height
        "MySpoutID",             // spoutId
        60                       // maxFps
    );
});