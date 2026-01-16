package top.yunmouren.craftbrowser.client.config;

import top.yunmouren.craftbrowser.Craftbrowser;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

public class Config {
    public static final Client CLIENT = new Client();

    public static class Client {
        public final ConfigSpec spec;
        public final ConfigSpec.ConfigValue<Integer> browserMaxfps;
        public final ConfigSpec.ConfigValue<Integer> keyPressDelay;
        public final ConfigSpec.ConfigValue<Integer> scrollWheelPixels;
        public final ConfigSpec.ConfigValue<Boolean> customizeRpc_IDEnabled;
        public final ConfigSpec.ConfigValue<String> customizeRpc_ID;
        public final ConfigSpec.ConfigValue<Integer> customizeDebugPort;
        public final ConfigSpec.ConfigValue<Boolean> externalHttpServer;
        public final ConfigSpec.ConfigValue<String> externalApiUrl;
        public final ConfigSpec.ConfigValue<Integer> externalHttpServerPort;

        public Client() {
            ConfigSpec.Builder builder = new ConfigSpec.Builder();

            // ------------------ Browser ------------------
            builder.push("Browser");
            browserMaxfps = builder.comment("Maximum browser frame rate [15-240]").define("MaxFps", 120, 15, 240);
            keyPressDelay = builder.comment("Key press delay in ms [0-1000]").define("KeyPressDelay", 200, 0, 1000);
            scrollWheelPixels = builder.comment("Scroll wheel step in pixels [1-1000]").define("ScrollWheelPixels", 150, 1, 1000);
            builder.pop();

            // ------------------ Debug ------------------
            builder.push("Debug");
            customizeRpc_IDEnabled = builder.define("CustomIdEnabled", false);
            customizeRpc_ID = builder.define("CustomId", "GLOBAL_NCEF");
            customizeDebugPort = builder.define("DebugPort", 0, 0, 65535);
            builder.pop();
            // ------------------ ExternalHttp ------------------
            builder.push("ExternalHttp"); // 配置文件的分组
            externalHttpServer = builder.comment("Enable external HTTP server").comment("Conflicts with customizeLoadingScreenUrl. When enabled, the URL will always be loaded from this HTTP server.").comment("Replace the dist folder in the JAR file").define("Enabled", false);
            externalHttpServerPort = builder.comment("External HTTP server port [0-65535]. Use 0 for a random port.").define("Port", 0, 0, 65535);
            externalApiUrl = builder.comment("Server only").define("externalApiUrl", "http://localhost:9000/api");
            builder.pop();
            this.spec = builder.build("craftbrowser_settings.toml");
        }

        public void load() {
            try {
                spec.load();
                this.save();
                if (!Config.CLIENT.customizeRpc_IDEnabled.get()) {
                    Config.CLIENT.customizeRpc_ID.set("NCEF_" + generateRandomString(5));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static String generateRandomString(int length) {
            final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            ThreadLocalRandom random = ThreadLocalRandom.current();
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            return sb.toString();
        }

        public void save() {
            try {
                spec.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

