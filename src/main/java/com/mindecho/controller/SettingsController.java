package com.mindecho.controller;

import com.mindecho.App;
import com.mindecho.service.LogStoreService;
import com.mindecho.util.ConfigManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class SettingsController {
    
    @FXML
    private TextField apiBaseUrlField;
    
    @FXML
    private TextField apiKeyField;
    
    @FXML
    private RadioButton lightThemeRadio;
    
    @FXML
    private RadioButton darkThemeRadio;
    
    @FXML
    private CheckBox audioEnabledCheck;
    
    @FXML
    private Label audioStatusLabel;
    
    @FXML
    private void initialize() {
        ConfigManager config = App.getServiceLocator().getConfigManager();
        
        String apiBaseUrl = config.get("openai.base.url");
        if (apiBaseUrl != null) {
            apiBaseUrlField.setText(apiBaseUrl);
        }
        
        String apiKey = config.get("openai.api.key");
        if (apiKey != null) {
            apiKeyField.setText(apiKey);
        }
        
        String currentTheme = config.getTheme();
        if ("dark".equals(currentTheme)) {
            darkThemeRadio.setSelected(true);
        } else {
            lightThemeRadio.setSelected(true);
        }
        
        boolean audioEnabled = config.isAudioEnabled();
        audioEnabledCheck.setSelected(audioEnabled);
        audioStatusLabel.setText(audioEnabled ? "\u266A 已开启" : "\u2639 已关闭");
        
        ToggleGroup themeGroup = new ToggleGroup();
        lightThemeRadio.setToggleGroup(themeGroup);
        darkThemeRadio.setToggleGroup(themeGroup);
    }
    
    @FXML
    private void saveSettings() {
        ConfigManager config = App.getServiceLocator().getConfigManager();

        String baseUrl = apiBaseUrlField.getText() == null ? "" : apiBaseUrlField.getText().trim();
        config.set("openai.base.url", baseUrl.isBlank()
                ? "https://api.openai.com/v1/chat/completions"
                : baseUrl);
        config.set("openai.api.key", apiKeyField.getText() == null ? "" : apiKeyField.getText().trim());

        String theme = lightThemeRadio.isSelected() ? "light" : "dark";
        config.set("app.theme", theme);

        boolean audioEnabled = audioEnabledCheck.isSelected();
        config.set("app.audio.enabled", String.valueOf(audioEnabled));
        config.save();

        syncAudioEngine(audioEnabled);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("保存成功");
        alert.setHeaderText(null);
        alert.setContentText("设置已保存！✨");
        alert.showAndWait();

        App.applyTheme();
    }

    @FXML
    private void handleThemeChange() {
        App.updateThemeStylesheet(lightThemeRadio.isSelected() ? "light" : "dark");
    }

    @FXML
    private void handleAudioToggle() {
        ConfigManager config = App.getServiceLocator().getConfigManager();
        boolean enabled = audioEnabledCheck.isSelected();
        config.set("app.audio.enabled", String.valueOf(enabled));
        config.save();

        syncAudioEngine(enabled);
        audioStatusLabel.setText(enabled ? "\u266A 已开启" : "\u2639 已关闭");
    }
    
    @FXML
    private void handleDeleteAll() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认销毁");
        confirmAlert.setHeaderText("确定要销毁全部数据吗？");
        confirmAlert.setContentText("此操作不可恢复！");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    LogStoreService logStore = App.getServiceLocator().getLogStoreService();
                    logStore.deleteAll();
                    
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("完成");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("全部数据已销毁 ✨");
                    successAlert.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @FXML
    private void goToMain() throws IOException {
        App.setRoot("main");
    }
    
    @FXML
    private void goToScratch() throws IOException {
        App.setRoot("scratch");
    }
    
    @FXML
    private void goToReport() throws IOException {
        App.setRoot("report");
    }
    
    @FXML
    private void goToTrash() throws IOException {
        App.setRoot("trash");
    }

    private void syncAudioEngine(boolean enabled) {
        com.mindecho.service.ResonanceAudioEngine audio = App.getServiceLocator().getResonanceAudioEngine();
        audio.setEnabled(enabled);
        if (enabled) {
            LogStoreService logStore = App.getServiceLocator().getLogStoreService();
            var todayLabels = logStore.findByDate(java.time.LocalDate.now()).stream()
                    .map(log -> log.getEmotionLabel())
                    .collect(java.util.stream.Collectors.toList());
            var weather = App.getServiceLocator().getWeatherEngine().compute(todayLabels);
            audio.resume(weather);
        }
    }
}
