package top.yunmouren.craftbrowser.client.browser.core;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BrowserInstance {

    private static Process process;

    public static Process getInstance() {
        return process;
    }

    public BrowserInstance() {
        startBrowserProcess();
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            Craftbrowser.LOGGER.info("WebViewSpoutCapture process stopped manually");
            process = null;
        }
    }

    public Process getProcess() {
        return process;
    }


    private void startBrowserProcess() {
        try {
            ProcessBuilder builder = getProcessBuilder();
            process = builder.start();

            Thread outputThread = new Thread(() -> readStream(process.getInputStream()));
            outputThread.setDaemon(true);
            outputThread.start();

            Craftbrowser.LOGGER.info("WebViewSpoutCapture started on port {}", Config.CLIENT.customizeBrowserPort.get());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                    try {
                        process.waitFor(5, TimeUnit.SECONDS); // 等待最多 5 秒
                    } catch (InterruptedException ignored) {
                    }
                    Craftbrowser.LOGGER.info("WebViewSpoutCapture process stopped on JVM exit");
                }
            }));

        } catch (IOException e) {
            Craftbrowser.LOGGER.error("Failed to start WebViewSpoutCapture: {}", e.getMessage());
        }
    }

    private void readStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Craftbrowser.LOGGER.info(line);
            }
        } catch (IOException e) {
            Craftbrowser.LOGGER.error("Error reading process stream: {}", e.getMessage());
        }
    }

    @NotNull
    private ProcessBuilder getProcessBuilder() {
        Path modsDir = Minecraft.getInstance().gameDirectory.toPath().resolve("NCEF");
        Path exePath = modsDir.resolve("NCEF.exe");

        ProcessBuilder builder = new ProcessBuilder(exePath.toString());
        builder.directory(modsDir.toFile());

        Map<String, String> env = builder.environment();
        env.put("LANG", "en_US.UTF-8");
        env.put("BROWSER_PORT", String.valueOf(Config.CLIENT.customizeBrowserPort.get()));
        env.put("SPOUT_ID", Config.CLIENT.customizeSpoutID.get());
        env.put("MAXFPS", Config.CLIENT.browserMaxfps.get().toString());
        env.put("CUSTOMIZE_LOADING_SCREEN_URL", Config.CLIENT.customizeLoadingScreenUrl.get());
        builder.inheritIO();
        return builder;
    }
}
