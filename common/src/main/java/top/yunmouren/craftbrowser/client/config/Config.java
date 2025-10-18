package top.yunmouren.craftbrowser.client.config;

import top.yunmouren.craftbrowser.Craftbrowser;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

public class Config {
    public static final Client CLIENT = new Client();

    public static class Client {
        // 配置规范对象
        public final ConfigSpec spec;

        // 所有配置值
        public final ConfigSpec.ConfigValue<Boolean> customizeLoadingScreenEnabled;
        public final ConfigSpec.ConfigValue<String> customizeLoadingScreenUrl;
        public final ConfigSpec.ConfigValue<Boolean> openCustomWebOnStart;
        public final ConfigSpec.ConfigValue<Boolean> useCustomURL;
        public final ConfigSpec.ConfigValue<String> customURL;
        public final ConfigSpec.ConfigValue<Integer> browserMaxfps;
        public final ConfigSpec.ConfigValue<Boolean> customizeBrowserPortEnabled;
        public final ConfigSpec.ConfigValue<Integer> customizeBrowserPort;
        public final ConfigSpec.ConfigValue<Integer> keyPressDelay;
        public final ConfigSpec.ConfigValue<Integer> scrollWheelPixels;
        public final ConfigSpec.ConfigValue<Boolean> customizeSpoutIDEnabled;
        public final ConfigSpec.ConfigValue<String> customizeSpoutID;
        public final ConfigSpec.ConfigValue<Boolean> externalHttpServer;
        public final ConfigSpec.ConfigValue<String> externalApiUrl;
        public final ConfigSpec.ConfigValue<Integer> externalHttpServerPort;

        public Client() {
            ConfigSpec.Builder builder = new ConfigSpec.Builder();

            // ------------------ Loading Screen ------------------
            builder.push("LoadingScreen");
            customizeLoadingScreenEnabled = builder
                    .comment("Enable custom loading screen")
                    .define("Enabled", false);
            customizeLoadingScreenUrl = builder
                    .comment("Custom loading screen URL")
                    .define("Url", "https://example.com");
            openCustomWebOnStart = builder
                    .comment("Open custom web page on game start")
                    .define("OpenOnStart", false);
            useCustomURL = builder
                    .comment("Use custom URL on start")
                    .define("UseCustomUrl", false);
            customURL = builder
                    .comment("Custom URL to open on start")
                    .define("CustomUrl", "https://example.com");
            builder.pop();

            // ------------------ Browser ------------------
            builder.push("Browser");
            browserMaxfps = builder
                    .comment("Maximum browser frame rate [15-240]")
                    .define("MaxFps", 120, 15, 240);
            customizeBrowserPortEnabled = builder
                    .comment("Enable custom browser port")
                    .define("CustomPortEnabled", false);
            customizeBrowserPort = builder
                    .comment("Custom browser port [0-65535]")
                    .define("CustomPort", 9222, 0, 65535);
            keyPressDelay = builder
                    .comment("Key press delay in ms [0-1000]")
                    .define("KeyPressDelay", 200, 0, 1000);
            scrollWheelPixels = builder
                    .comment("Scroll wheel step in pixels [1-1000]")
                    .define("ScrollWheelPixels", 150, 1, 1000);
            builder.pop();

            // ------------------ Spout ------------------
            builder.push("Spout");
            customizeSpoutIDEnabled = builder
                    .comment("Enable custom spout ID")
                    .define("CustomIdEnabled", false);
            customizeSpoutID = builder
                    .comment("Custom spout ID")
                    .define("CustomId", "NCEF");
            builder.pop();
            builder.push("ExternalHttp"); // 配置文件的分组
            externalHttpServer = builder.comment("Enable external HTTP server")
                    .comment("Conflicts with customizeLoadingScreenUrl. When enabled, the URL will always be loaded from this HTTP server.")
                    .define("Enabled", false);
            externalHttpServerPort = builder.comment("External HTTP server port [0-65535]. Use 0 for a random port.")
                    .define("Port", 0, 0, 65535);
            externalApiUrl = builder
                    .comment("The URL of the external HTTP API to send Minecraft data to.")
                    .comment("POST requests to the /api path on the HTTP SERVER URL will be converted into Minecraft packets and forwarded to the backend server.")
                    .comment("Example: Client POST http://localhost:8000/api")
                    .comment("The POST request will ultimately be sent to the server at http://localhost:9000/api")
                    .define("externalApiUrl", "http://localhost:9000/api");

            builder.pop();
            this.spec = builder.build("craftbrowser_settings.toml");
        }


        /**
         * 加载配置
         */
        public void load() {
            try {
                spec.load();
                this.save();
                if (!Config.CLIENT.customizeBrowserPortEnabled.get()) {
                    Config.CLIENT.customizeBrowserPort.set(generateRandomPort());
                }
                if (!Config.CLIENT.customizeSpoutIDEnabled.get()) {
                    Config.CLIENT.customizeSpoutID.set("WebViewSpoutCapture_" + generateRandomString(10));
                }
                if (!Config.CLIENT.customizeLoadingScreenEnabled.get()) {
                    Config.CLIENT.customizeLoadingScreenUrl.set("https://example.com/");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static String generateRandomString(int length) {
            final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            ThreadLocalRandom random = ThreadLocalRandom.current();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return sb.toString();
        }

        private static int generateRandomPort() {
            for (int i = 0; i < 10; i++) { // 最多重试 10 次
                try (ServerSocket socket = new ServerSocket(0)) {
                    return socket.getLocalPort();
                } catch (IOException ignored) {
                }
            }
            Craftbrowser.LOGGER.error("Failed to get random port after multiple attempts, using fallback port 9222");
            return 9222; // 失败时使用默认端口
        }

        /**
         * 保存配置
         */
        public void save() {
            try {
                spec.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

