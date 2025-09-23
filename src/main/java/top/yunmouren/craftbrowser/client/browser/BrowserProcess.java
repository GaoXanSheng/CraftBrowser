package top.yunmouren.craftbrowser.client.browser;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import top.yunmouren.craftbrowser.Craftbrowser;
import top.yunmouren.craftbrowser.client.config.Config;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 管理 WebViewSpoutCapture 子进程
 */
public class BrowserProcess {

    private Process process;

    public BrowserProcess() {
        startBrowserProcess();
    }

    // ------------------- 公共方法 -------------------

    /** 手动停止子进程 */
    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            Craftbrowser.LOGGER.info("WebViewSpoutCapture process stopped manually");
            process = null;
        }
    }

    /** 获取子进程对象 */
    public Process getProcess() {
        return process;
    }

    // ------------------- 内部方法 -------------------

    private void startBrowserProcess() {
        try {
            ProcessBuilder builder = getProcessBuilder();
            process = builder.start();

            // 启动线程读取子进程输出
            Thread outputThread = new Thread(() -> readStream(process.getInputStream()));
            outputThread.setDaemon(true);
            outputThread.start();

            Craftbrowser.LOGGER.info("WebViewSpoutCapture started on port {}", Config.CLIENT.customizeBrowserPort.get());

            // JVM 退出时强制销毁
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                    try {
                        process.waitFor(5, TimeUnit.SECONDS); // 等待最多 5 秒
                    } catch (InterruptedException ignored) {}
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
        env.put("MAXFPS",Config.CLIENT.browserMaxfps.get().toString());
        env.put("CUSTOMIZE_LOADING_SCREEN_URL", Config.CLIENT.customizeLoadingScreenUrl.get());
        builder.inheritIO(); // 输出到父进程控制台
        return builder;
    }


}
