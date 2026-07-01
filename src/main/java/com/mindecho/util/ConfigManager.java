package com.mindecho.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {
    private static final Path CONFIG_DIR = resolveConfigDir();
    private static final Path CONFIG_PATH = CONFIG_DIR.resolve("config.properties");

    private final Properties properties;

    private static Path resolveConfigDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Path.of(appData, "MindEcho");
        }
        return Path.of(System.getProperty("user.home"), "AppData", "Roaming", "MindEcho");
    }

    public ConfigManager() {
        this.properties = new Properties();
        load();
    }

    private void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (FileInputStream fis = new FileInputStream(CONFIG_PATH.toFile())) {
                properties.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setDefaults();
            save();
        }
    }

    private void setDefaults() {
        properties.setProperty("app.theme", "light");
        properties.setProperty("app.audio.enabled", "true");
        properties.setProperty("openai.base.url", "https://api.openai.com/v1/chat/completions");
        properties.setProperty("openai.model", "gpt-4o-mini");
        properties.setProperty("openai.max_tokens", "100");
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH.toFile())) {
            properties.store(fos, "MindEcho Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public boolean isApiKeyConfigured() {
        String apiKey = get("openai.api.key");
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean isAudioEnabled() {
        return Boolean.parseBoolean(get("app.audio.enabled", "true"));
    }

    public String getTheme() {
        return get("app.theme", "light");
    }
}
