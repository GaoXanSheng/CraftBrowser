package top.yunmouren.craftbrowser.client.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(builder);
        CLIENT_SPEC = builder.build();
    }

    public static class Client {
        public Client(ForgeConfigSpec.Builder builder) {
            CustomizeLoadingScreenConfig(builder);
            BrowserConfig(builder);
            SpoutConfig(builder);
        }

        public ForgeConfigSpec.BooleanValue customizeLoadingScreenEnabled;
        public ForgeConfigSpec.ConfigValue<String> customizeLoadingScreenUrl;

        public void CustomizeLoadingScreenConfig(ForgeConfigSpec.Builder builder) {
            builder.push("CustomizeLoadingScreen");
            customizeLoadingScreenEnabled = builder
                    .comment("Do you use loading screen instead of example")
                    .define("CustomizeLoadingScreenEnabled", false);
            customizeLoadingScreenUrl = builder
                    .comment("Customize the web address for loading the screen")
                    .define("CustomizeLoadingScreenUrl", "https://example.com");
            builder.pop();
        }

        public ForgeConfigSpec.IntValue browserMaxfps;
        public ForgeConfigSpec.BooleanValue customizeBrowserPortEnabled;
        public ForgeConfigSpec.IntValue customizeBrowserPort;

        public void BrowserConfig(ForgeConfigSpec.Builder builder) {
            builder.push("BrowserConfig");
            browserMaxfps = builder.comment("Maximum frame rate of browser").defineInRange("MaxFps", 120, 30, 240);
            customizeBrowserPortEnabled = builder
                    .comment("Do you use customize browser port instead of random")
                    .define("CustomizeBrowserPortEnabled", false);
            customizeBrowserPort = builder.comment("customizeBrowserPort").defineInRange("customizeBrowserPort", 9222, 0, 65535);
            builder.pop();
        }

        public ForgeConfigSpec.BooleanValue customizeSpoutIDEnabled;
        public ForgeConfigSpec.ConfigValue<String> customizeSpoutID;

        public void SpoutConfig(ForgeConfigSpec.Builder builder) {
            builder.push("SpoutConfig");
            customizeSpoutIDEnabled = builder
                    .comment("Do you use spout ID instead of random")
                    .define("CustomizeSpoutIDEnabled", false);
            customizeSpoutID = builder.comment("Customize the spout ID").define("CustomizeSpoutID", "");
            builder.pop();
        }
    }
}
