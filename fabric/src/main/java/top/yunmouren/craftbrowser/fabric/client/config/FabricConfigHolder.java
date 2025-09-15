package top.yunmouren.craftbrowser.fabric.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import top.yunmouren.craftbrowser.client.browser.BrowserProcess;
import top.yunmouren.craftbrowser.config.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FabricConfigHolder {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "craftbrowser_settings.json";
    private static File CONFIG_FILE;

    public static void register(File configDir) {
        CONFIG_FILE = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
        new BrowserProcess(); // Fabric 下可以直接启动 BrowserProcess
    }

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            saveDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            applyConfig(data);
        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void saveDefaultConfig() {
        ConfigData data = new ConfigData(); // 默认值
        applyConfig(data);
        saveConfig(data);
    }

    public static void saveConfig(ConfigData data) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void applyConfig(ConfigData data) {
        Config.CLIENT.customizeLoadingScreenEnabled = data.customizeLoadingScreenEnabled;
        Config.CLIENT.customizeLoadingScreenUrl = data.customizeLoadingScreenUrl;
        Config.CLIENT.browserMaxfps = data.browserMaxfps;
        Config.CLIENT.customizeBrowserPortEnabled = data.customizeBrowserPortEnabled;
        Config.CLIENT.customizeBrowserPort = data.customizeBrowserPort;
        Config.CLIENT.customizeSpoutIDEnabled = data.customizeSpoutIDEnabled;
        Config.CLIENT.customizeSpoutID = data.customizeSpoutID;
    }

    public static class ConfigData {
        public boolean customizeLoadingScreenEnabled = false;
        public String customizeLoadingScreenUrl = "https://example.com";
        public int browserMaxfps = 120;
        public boolean customizeBrowserPortEnabled = false;
        public int customizeBrowserPort = 9222;
        public boolean customizeSpoutIDEnabled = false;
        public String customizeSpoutID = "";
    }
}
