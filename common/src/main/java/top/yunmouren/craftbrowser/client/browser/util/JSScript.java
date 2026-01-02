package top.yunmouren.craftbrowser.client.browser.util;

public class JSScript {
    public static String CreateBrowser(
            String url, int width, int height,
            String spoutID, int maxFps
    ) {
        return """
        (function () {
            return CefSharp.BindObjectAsync('AppController')
                .then(() => {
                    return AppController.createBrowser(
                        "%s", %d, %d, "%s", %d
                    );
                });
        })()
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
        (function () {
            const volume = %s;

            // Existing <audio> and <video>
            document.querySelectorAll('audio, video').forEach(elem => {
                elem.volume = volume;
            });

            // Override Audio constructor
            const OriginalAudio = window.Audio;
            window.Audio = function (...args) {
                const audio = new OriginalAudio(...args);
                audio.volume = volume;
                return audio;
            };
            window.Audio.prototype = OriginalAudio.prototype;

            // Override AudioContext
            const OriginalAudioContext =
                window.AudioContext || window.webkitAudioContext;

            if (OriginalAudioContext) {
                window.AudioContext = function (...args) {
                    const ctx = new OriginalAudioContext(...args);

                    const master = ctx.createGain();
                    master.gain.value = volume;

                    const originalDestination = ctx.destination;
                    master.connect(originalDestination);

                    ctx.destination = master;
                    return ctx;
                };

                window.AudioContext.prototype =
                    OriginalAudioContext.prototype;
            }
        })();
        """.formatted(volStr);
    }

}
