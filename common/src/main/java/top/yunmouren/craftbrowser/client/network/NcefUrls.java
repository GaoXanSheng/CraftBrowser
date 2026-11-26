package top.yunmouren.craftbrowser.client.network;

import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;

public final class NcefUrls {
    public static final String SCHEME = "ncef://";
    private static final String DIST_DIR = "dist";
    private static final String DEFAULT_PAGE = "index.html";

    private NcefUrls() {
    }

    public static Path distRoot() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("NCEF").resolve(DIST_DIR).toAbsolutePath().normalize();
    }

    public static String resolve(String url) {
        if (url == null || !url.startsWith(SCHEME)) return url;

        String path = url.substring(SCHEME.length());
        String suffix = "";
        int query = path.indexOf('?');
        if (query >= 0) {
            suffix = path.substring(query);
            path = path.substring(0, query);
        }
        if (path.isEmpty()) path = DEFAULT_PAGE;

        Path root = distRoot();
        Path file = root.resolve(path).normalize();
        if (!file.startsWith(root)) return url;
        if (!Files.isRegularFile(file)) return url;

        return file.toUri() + suffix;
    }
}
