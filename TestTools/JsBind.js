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

Inject a JavaScript script to control the volume when the page loads
demo :
(function() {
    const volume = %s;
    document.querySelectorAll('audio, video').forEach(elem => {
        elem.volume = volume;
    });
    const OriginalAudio = window.Audio;
    window.Audio = function(...args) {
        const audio = new OriginalAudio(...args);
        audio.volume = volume;
        return audio;
    };
    window.Audio.prototype = OriginalAudio.prototype;
    const OriginalAudioContext = window.AudioContext || window.webkitAudioContext;
    if (OriginalAudioContext) {
        window.AudioContext = function(...args) {
            const ctx = new OriginalAudioContext(...args);
            const master = ctx.createGain();
            master.gain.value = volume;
            const originalConnect = ctx.destination.connect || ctx.destination.__proto__.connect;
            const originalDestination = ctx.destination;
            ctx.destination = master;
            master.connect(originalDestination);
            return ctx;
        };
        window.AudioContext.prototype = OriginalAudioContext.prototype;
    }
})();
