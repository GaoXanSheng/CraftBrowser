package top.yunmouren.craftbrowser.client.config;

import net.minecraft.client.Minecraft;
import top.yunmouren.craftbrowser.Craftbrowser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigSpec {
    private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();
    private final Path filePath;

    private ConfigSpec(Path filePath, Map<String, ConfigValue<?>> values) {
        this.filePath = filePath;
        this.values.putAll(values);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigValue<T> get(String key) {
        return (ConfigValue<T>) values.get(key);
    }

    /** 保存到 TOML */
    public void save() throws IOException {
        Files.createDirectories(filePath.getParent());
        Map<String, List<Map.Entry<String, ConfigValue<?>>>> sections = new LinkedHashMap<>();

        // 按点号分组
        for (Map.Entry<String, ConfigValue<?>> e : values.entrySet()) {
            String key = e.getKey();
            String section = key.contains(".") ? key.substring(0, key.lastIndexOf('.')) : "";
            sections.computeIfAbsent(section, s -> new ArrayList<>()).add(e);
        }

        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, List<Map.Entry<String, ConfigValue<?>>>> entry : sections.entrySet()) {
            String section = entry.getKey();
            if (!section.isEmpty()) lines.add("[" + section + "]");

            for (Map.Entry<String, ConfigValue<?>> kv : entry.getValue()) {
                ConfigValue<?> cv = kv.getValue();
                if (cv.comment != null) lines.add("# " + cv.comment);
                String simpleKey = kv.getKey().contains(".") ? kv.getKey().substring(kv.getKey().lastIndexOf('.') + 1) : kv.getKey();
                lines.add(simpleKey + " = " + formatTomlValue(cv.get()));
            }
            lines.add("");
        }

        Files.write(filePath, lines);
    }
    /** 从 TOML 文件加载 */
    public void load() throws IOException {
        // 确保目录存在
        Files.createDirectories(filePath.getParent());

        if (!Files.exists(filePath)) {
            save(); // 文件不存在就写入默认值
            return;
        }

        List<String> lines = Files.readAllLines(filePath);
        String currentSection = "";
        for (String line : lines) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;

            // 支持 [Section] TOML 标记
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).trim();
                continue;
            }

            // key = value
            String[] parts = line.split("=", 2);
            if (parts.length != 2) continue;
            String key = parts[0].trim();
            String raw = parts[1].trim();

            // 合成完整 key：Section.Key
            String fullKey = currentSection.isEmpty() ? key : currentSection + "." + key;

            ConfigValue<?> cv = values.get(fullKey);
            if (cv != null) {
                cv.set(parseTomlValue(raw, cv.getDefault().getClass()));
            } else {
                Craftbrowser.LOGGER.error("unmatched Configuration key: {} -> {}", fullKey, raw);
            }
        }
    }

    private static String formatTomlValue(Object v) {
        if (v instanceof String) return "\"" + v + "\"";
        return v.toString();
    }

    private static Object parseTomlValue(String raw, Class<?> type) {
        if (type == String.class) return raw.replaceAll("^\"|\"$", "");
        if (type == int.class || type == Integer.class) return Integer.parseInt(raw);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(raw);
        return raw;
    }

    // -------------------
    // Builder
    // -------------------
    public static class Builder {
        private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();
        private final Deque<String> sectionStack = new ArrayDeque<>();
        private String pendingComment;
        private String pendingTranslation;

        private String fullKey(String key) {
            if (sectionStack.isEmpty()) return key;
            List<String> parts = new ArrayList<>();
            sectionStack.descendingIterator().forEachRemaining(parts::add);
            return String.join(".", parts) + "." + key;
        }

        public Builder push(String section) {
            sectionStack.push(section);
            return this;
        }

        public Builder pop() {
            if (!sectionStack.isEmpty()) sectionStack.pop();
            return this;
        }

        public Builder comment(String comment) {
            this.pendingComment = comment;
            return this;
        }

        public Builder translation(String translation) {
            this.pendingTranslation = translation;
            return this;
        }

        public ConfigValue<Boolean> define(String key, boolean defaultValue) {
            var cv = new ConfigValue<>(defaultValue);
            cv.comment = pendingComment;
            cv.translation = pendingTranslation;
            values.put(fullKey(key), cv);
            resetPending();
            return cv;
        }

        public ConfigValue<String> define(String key, String defaultValue) {
            var cv = new ConfigValue<>(defaultValue);
            cv.comment = pendingComment;
            cv.translation = pendingTranslation;
            values.put(fullKey(key), cv);
            resetPending();
            return cv;
        }

        public ConfigValue<Integer> defineInRange(String key, int defaultValue, int min, int max) {
            var cv = new ConfigValue<>(defaultValue, min, max);
            cv.comment = pendingComment;
            cv.translation = pendingTranslation;
            values.put(fullKey(key), cv);
            resetPending();
            return cv;
        }

        private void resetPending() {
            this.pendingComment = null;
            this.pendingTranslation = null;
        }

        /**
         * 自动定位到 .minecraft/config/
         */
        public ConfigSpec build(String fileName) {
            Path configDir = Minecraft.getInstance().gameDirectory.toPath().resolve("config");
            Path filePath = configDir.resolve(fileName);
            return new ConfigSpec(filePath, values);
        }
    }

    // -------------------
    // ConfigValue
    // -------------------
    public static class ConfigValue<T> {
        private final T defaultValue;
        private final Integer min, max;
        private T value;

        private String comment;
        private String translation;

        public ConfigValue(T defaultValue) {
            this(defaultValue, null, null);
        }

        public ConfigValue(T defaultValue, Integer min, Integer max) {
            this.defaultValue = defaultValue;
            this.value = defaultValue;
            this.min = min;
            this.max = max;
        }

        public T get() {
            return value;
        }

        public void set(Object newVal) {
            try {
                @SuppressWarnings("unchecked")
                T casted = (T) newVal;
                if (casted instanceof Integer && min != null && max != null) {
                    int i = (Integer) casted;
                    if (i < min || i > max) {
                        Craftbrowser.LOGGER.error("Config value out of range: {}", i);
                        return;
                    }
                }
                this.value = casted;
            } catch (ClassCastException e) {
                Craftbrowser.LOGGER.error("Invalid type for config value: {}", newVal);
            }
        }

        public T getDefault() {
            return defaultValue;
        }
    }
}
