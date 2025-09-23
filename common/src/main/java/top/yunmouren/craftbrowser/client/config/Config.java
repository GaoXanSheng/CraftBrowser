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

        public final ConfigSpec.ConfigValue<Integer> browserMaxfps;
        public final ConfigSpec.ConfigValue<Boolean> customizeBrowserPortEnabled;
        public final ConfigSpec.ConfigValue<Integer> customizeBrowserPort;

        public final ConfigSpec.ConfigValue<Boolean> customizeSpoutIDEnabled;
        public final ConfigSpec.ConfigValue<String> customizeSpoutID;

        public Client() {
            ConfigSpec.Builder builder = new ConfigSpec.Builder();

            builder.push("CustomizeLoadingScreen");
            customizeLoadingScreenEnabled = builder
                    .comment("Do you use loading screen instead of example")
                    .define("CustomizeLoadingScreenEnabled", false);
            customizeLoadingScreenUrl = builder
                    .comment("Customize the web address for loading the screen")
                    .define("CustomizeLoadingScreenUrl", "");
            builder.pop();

            builder.push("BrowserConfig");
            browserMaxfps = builder.comment("Maximum frame rate of browser").defineInRange("MaxFps", 120, 30, 240);
            customizeBrowserPortEnabled = builder
                    .comment("Do you use customize spout ID instead of random")
                    .define("CustomizeBrowserPortEnabled", false);
            customizeBrowserPort = builder.comment("customizeBrowserPort").defineInRange("customizeBrowserPort", 9222, 0, 65535);
            builder.pop();

            builder.push("SpoutConfig");
            customizeSpoutIDEnabled = builder
                    .comment("Do you use spout ID instead of random")
                    .define("CustomizeSpoutIDEnabled",false);
            customizeSpoutID = builder.comment("Customize the spout ID").define("CustomizeSpoutID","");
            builder.pop();

            this.spec = builder.build("craftbrowser_settings.toml");
        }

        /** 加载配置 */
        public void load() {
            try {
                spec.load();
                if (!Config.CLIENT.customizeBrowserPortEnabled.get()) {
                    Config.CLIENT.customizeBrowserPort.set(generateRandomPort());
                }
                if (!Config.CLIENT.customizeSpoutIDEnabled.get()){
                    Config.CLIENT.customizeSpoutID.set("WebViewSpoutCapture_" + generateRandomString(10));
                }
                if (!Config.CLIENT.customizeLoadingScreenEnabled.get()){
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
                } catch (IOException ignored) { }
            }
            Craftbrowser.LOGGER.error("Failed to get random port after multiple attempts, using fallback port 9222");
            return 9222; // 失败时使用默认端口
        }
        /** 保存配置 */
        public void save() {
            try {
                spec.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

