package top.yunmouren.craftbrowser.client.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import top.yunmouren.craftbrowser.client.browser.BrowserProcess;
import top.yunmouren.craftbrowser.config.Config;

public class ForgeConfigHolder {
    public static final ForgeConfigSpec CLIENT_SPEC;
    private static final ClientSpec CLIENT_SPEC_IMPL;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CLIENT_SPEC_IMPL = new ClientSpec(builder);
        CLIENT_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "craftbrowser_settings.toml");

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener((ModConfigEvent event) -> {
            if (event.getConfig().getSpec() == CLIENT_SPEC) {
                syncFromForge();
                new BrowserProcess();
            }
        });
    }

    private static void syncFromForge() {
        Config.CLIENT.customizeLoadingScreenEnabled = CLIENT_SPEC_IMPL.customizeLoadingScreenEnabled.get();
        Config.CLIENT.customizeLoadingScreenUrl = CLIENT_SPEC_IMPL.customizeLoadingScreenUrl.get();
        Config.CLIENT.browserMaxfps = CLIENT_SPEC_IMPL.browserMaxfps.get();
        Config.CLIENT.customizeBrowserPortEnabled = CLIENT_SPEC_IMPL.customizeBrowserPortEnabled.get();
        Config.CLIENT.customizeBrowserPort = CLIENT_SPEC_IMPL.customizeBrowserPort.get();
        Config.CLIENT.customizeSpoutIDEnabled = CLIENT_SPEC_IMPL.customizeSpoutIDEnabled.get();
        Config.CLIENT.customizeSpoutID = CLIENT_SPEC_IMPL.customizeSpoutID.get();
    }

    private static class ClientSpec {
        public final ForgeConfigSpec.BooleanValue customizeLoadingScreenEnabled;
        public final ForgeConfigSpec.ConfigValue<String> customizeLoadingScreenUrl;
        public final ForgeConfigSpec.IntValue browserMaxfps;
        public final ForgeConfigSpec.BooleanValue customizeBrowserPortEnabled;
        public final ForgeConfigSpec.IntValue customizeBrowserPort;
        public final ForgeConfigSpec.BooleanValue customizeSpoutIDEnabled;
        public final ForgeConfigSpec.ConfigValue<String> customizeSpoutID;

        public ClientSpec(ForgeConfigSpec.Builder builder) {
            builder.push("CustomizeLoadingScreen");
            customizeLoadingScreenEnabled = builder
                    .comment("Do you use loading screen instead of example")
                    .define("CustomizeLoadingScreenEnabled", Config.CLIENT.customizeLoadingScreenEnabled);
            customizeLoadingScreenUrl = builder
                    .comment("Customize the web address for loading the screen")
                    .define("CustomizeLoadingScreenUrl", Config.CLIENT.customizeLoadingScreenUrl);
            builder.pop();

            builder.push("BrowserConfig");
            browserMaxfps = builder.comment("Maximum frame rate of browser").defineInRange("MaxFps", Config.CLIENT.browserMaxfps, 30, 240);
            customizeBrowserPortEnabled = builder
                    .comment("Do you use customize browser port instead of random")
                    .define("CustomizeBrowserPortEnabled", Config.CLIENT.customizeBrowserPortEnabled);
            customizeBrowserPort = builder.comment("customizeBrowserPort").defineInRange("customizeBrowserPort", Config.CLIENT.customizeBrowserPort, 0, 65535);
            builder.pop();

            builder.push("SpoutConfig");
            customizeSpoutIDEnabled = builder
                    .comment("Do you use spout ID instead of random")
                    .define("CustomizeSpoutIDEnabled", Config.CLIENT.customizeSpoutIDEnabled);
            customizeSpoutID = builder.comment("Customize the spout ID").define("CustomizeSpoutID", Config.CLIENT.customizeSpoutID);
            builder.pop();
        }
    }
}
