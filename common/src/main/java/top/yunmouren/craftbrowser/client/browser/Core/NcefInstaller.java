package top.yunmouren.craftbrowser.client.browser.Core;

import org.jetbrains.annotations.Nullable;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class NcefInstaller {
    private static final String RESOURCE_ROOT = "/win64/";
    private static final String INDEX = "files.txt";
    private static final String VERSION = "version.txt";
    private static final String STAMP = ".version";

    private static final String DIST_INDEX = "/ncef-dist.txt";
    private static final String DIST_ROOT = "/dist/";
    private static final String DIST_MARKER = "dist/index.html";

    private NcefInstaller() {
    }

    public static boolean ensureInstalled(Path gameDir) {
        Path target = gameDir.resolve("NCEF").toAbsolutePath().normalize();

        String bundledVersion = readText(RESOURCE_ROOT + VERSION);
        if (bundledVersion == null || bundledVersion.isBlank()) {
            Craftbrowser.LOGGER.warn("[CraftBrowser] No bundled NCEF in the mod jar, expecting {} to exist already", target);
            return Files.isRegularFile(target.resolve("NCEF.exe"));
        }

        boolean launcherOk = Files.isRegularFile(target.resolve("NCEF.exe"));
        boolean distOk = Files.isRegularFile(target.resolve(DIST_MARKER));
        if (launcherOk && distOk && bundledVersion.equals(readFile(target.resolve(STAMP)))) {
            return true;
        }

        URL manifestUrl = NcefInstaller.class.getResource(RESOURCE_ROOT + INDEX);
        List<String> entries = readLines(RESOURCE_ROOT + INDEX);
        if (manifestUrl == null || entries == null) {
            Craftbrowser.LOGGER.error("[CraftBrowser] Bundled NCEF has no {}", INDEX);
            return false;
        }
        Craftbrowser.LOGGER.info("[CraftBrowser] Bundled NCEF manifest at {} lists {} files", manifestUrl, entries.size());

        int written = extractNcef(target, manifestUrl, entries);
        int distWritten = 0;
        try {
            distWritten = extractDist(target);
        } catch (IOException e) {
            Craftbrowser.LOGGER.error("[CraftBrowser] Failed to extract the dist pages into {}", target, e);
        }

        boolean ok = written > 0 && Files.isRegularFile(target.resolve("NCEF.exe")) && distWritten > 0;
        if (ok) {
            try {
                Files.writeString(target.resolve(STAMP), bundledVersion, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Craftbrowser.LOGGER.warn("[CraftBrowser] Could not write the version stamp: {}", e.getMessage());
            }
        }

        Craftbrowser.LOGGER.info("[CraftBrowser] Extracted {} NCEF files and {} dist files into {} (version {})",
                written, distWritten, target, bundledVersion);
        return Files.isRegularFile(target.resolve("NCEF.exe"));
    }

    private static int extractNcef(Path target, URL manifestUrl, List<String> entries) {
        if (entries.isEmpty()) {
            Craftbrowser.LOGGER.error("[CraftBrowser] Bundled NCEF manifest is empty, nothing to extract into {}", target);
            return 0;
        }

        try {
            Files.createDirectories(target);
            int written = 0;
            int failed = 0;
            for (String relative : entries) {
                if (relative.isBlank()) continue;
                Path out = target.resolve(relative).normalize();
                if (!out.startsWith(target)) {
                    failed++;
                    Craftbrowser.LOGGER.warn("[CraftBrowser] Refusing to write outside {}: {}", target, relative);
                    continue;
                }
                try (InputStream in = openEntry(manifestUrl, relative)) {
                    if (in == null) {
                        Craftbrowser.LOGGER.warn("[CraftBrowser] Missing bundled resource {}", relative);
                        failed++;
                        continue;
                    }
                    if (out.getParent() != null) Files.createDirectories(out.getParent());
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                    written++;
                } catch (IOException e) {
                    failed++;
                    Craftbrowser.LOGGER.warn("[CraftBrowser] Could not write {}: {}", out, e.getMessage());
                }
            }
            if (written == 0) {
                Craftbrowser.LOGGER.error("[CraftBrowser] Extracted no files; the manifest was read from {}", manifestUrl);
            }
            if (failed > 0) {
                Craftbrowser.LOGGER.warn("[CraftBrowser] {} NCEF files could not be written", failed);
            }
            return written;
        } catch (IOException e) {
            Craftbrowser.LOGGER.error("[CraftBrowser] Failed to extract NCEF into {}", target, e);
            return 0;
        }
    }

    private static int extractDist(Path target) throws IOException {
        List<String> entries = readLines(DIST_INDEX);
        if (entries == null || entries.isEmpty()) {
            Craftbrowser.LOGGER.warn("[CraftBrowser] No bundled dist pages found");
            return 0;
        }

        Path root = target.resolve("dist");
        Files.createDirectories(root);
        int written = 0;
        for (String relative : entries) {
            if (relative.isBlank()) continue;
            Path out = root.resolve(relative).normalize();
            if (!out.startsWith(root)) continue;
            try (InputStream in = NcefInstaller.class.getResourceAsStream(DIST_ROOT + relative)) {
                if (in == null) continue;
                if (out.getParent() != null) Files.createDirectories(out.getParent());
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                written++;
            }
        }
        return written;
    }

    @Nullable
    private static InputStream openEntry(URL manifestUrl, String relative) {
        try {
            return new URL(manifestUrl, relative).openStream();
        } catch (IOException ignored) {
            return NcefInstaller.class.getResourceAsStream(RESOURCE_ROOT + relative);
        }
    }

    @Nullable
    private static String readText(String resourcePath) {
        URL url = NcefInstaller.class.getResource(resourcePath);
        if (url == null) return null;
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private static List<String> readLines(String resourcePath) {
        String content = readText(resourcePath);
        if (content == null) return null;
        List<String> lines = new ArrayList<>();
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) lines.add(trimmed);
        }
        return lines;
    }

    @Nullable
    private static String readFile(Path path) {
        try {
            return Files.isRegularFile(path) ? Files.readString(path, StandardCharsets.UTF_8).trim() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
