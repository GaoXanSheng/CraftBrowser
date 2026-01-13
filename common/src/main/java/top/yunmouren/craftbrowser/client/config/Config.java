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

        public Client() {
            ConfigSpec.Builder builder = new ConfigSpec.Builder();

            // ------------------ Browser ------------------
            builder.push("Browser");
            browserMaxfps = builder
                    .comment("Maximum browser frame rate [15-240]")
                    .define("MaxFps", 120, 15, 240);
            keyPressDelay = builder
                    .comment("Key press delay in ms [0-1000]")
                    .define("KeyPressDelay", 200, 0, 1000);
            scrollWheelPixels = builder
                    .comment("Scroll wheel step in pixels [1-1000]")
                    .define("ScrollWheelPixels", 150, 1, 1000);
            builder.pop();

            // ------------------ Debug ------------------
            builder.push("Debug");
            customizeRpc_IDEnabled = builder
                    .define("CustomIdEnabled", false);
            customizeRpc_ID = builder
                    .define("CustomId", "NCEF");
            builder.pop();
            this.spec = builder.build("craftbrowser_settings.toml");
        }

        public void load() {
            try {
                spec.load();
                this.save();
                if (!Config.CLIENT.customizeRpc_IDEnabled.get()) {
                    Config.CLIENT.customizeRpc_ID.set("GLOBAL_NCEF");
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

