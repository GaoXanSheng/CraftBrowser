package top.yunmouren.craftbrowser.config;

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
                    .comment("Do you use customize browser port instead of random")
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
            } catch (Exception e) {
                e.printStackTrace();
            }
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

