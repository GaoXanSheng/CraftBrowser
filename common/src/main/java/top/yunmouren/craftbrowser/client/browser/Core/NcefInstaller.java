package top.yunmouren.craftbrowser.client.browser.Core;

import org.jetbrains.annotations.Nullable;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.io.IOException;
import java.io.InputStream;
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

    private NcefInstaller() {
    }

    public static boolean ensureInstalled(Path gameDir) {
        Path target = gameDir.resolve("NCEF");

        String bundledVersion = readResource(VERSION);
        if (bundledVersion == null) {
            Craftbrowser.LOGGER.warn("[CraftBrowser] No bundled NCEF in the mod jar, expecting {} to exist already", target);
            return Files.isRegularFile(target.resolve("NCEF.exe"));
        }

        if (bundledVersion.equals(readFile(target.resolve(STAMP))) && Files.isRegularFile(target.resolve("NCEF.exe"))) {
            return true;
        }

        List<String> entries = readLines(INDEX);
        if (entries == null) {
            Craftbrowser.LOGGER.error("[CraftBrowser] Bundled NCEF has no {}", INDEX);
            return false;
        }

        try {
            Files.createDirectories(target);
            int written = 0;
            int failed = 0;
            for (String relative : entries) {
                if (relative.isBlank()) continue;
                Path out = target.resolve(relative).normalize();
                if (!out.startsWith(target)) continue;
                try (InputStream in = NcefInstaller.class.getResourceAsStream(RESOURCE_ROOT + relative)) {
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
            if (failed == 0) {
                Files.writeString(target.resolve(STAMP), bundledVersion, StandardCharsets.UTF_8);
            }
            Craftbrowser.LOGGER.info("[CraftBrowser] Extracted {} NCEF files into {} (version {}, {} failed)",
                    written, target, bundledVersion, failed);
            return Files.isRegularFile(target.resolve("NCEF.exe"));
        } catch (IOException e) {
            Craftbrowser.LOGGER.error("[CraftBrowser] Failed to extract NCEF into {}", target, e);
            return false;
        }
    }

    @Nullable
    private static String readResource(String name) {
        try (InputStream in = NcefInstaller.class.getResourceAsStream(RESOURCE_ROOT + name)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private static List<String> readLines(String name) {
        String content = readResource(name);
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
