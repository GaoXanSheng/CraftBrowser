package top.yunmouren.craftbrowser.config;

public class Config {
    public static Client CLIENT = new Client();

    public static class Client {
        public boolean customizeLoadingScreenEnabled = false;
        public String customizeLoadingScreenUrl = "https://example.com";

        public int browserMaxfps = 120;
        public boolean customizeBrowserPortEnabled = false;
        public int customizeBrowserPort = 9222;

        public boolean customizeSpoutIDEnabled = false;
        public String customizeSpoutID = "";
    }
}
