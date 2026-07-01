package com.mindecho.controller;

import com.mindecho.App;
import com.mindecho.model.AiResponse;
import com.mindecho.model.DestructionLog;
import com.mindecho.model.EmotionWeather;
import com.mindecho.service.LogStoreService;
import com.mindecho.service.WeatherEngine;
import com.mindecho.ui.ShredParticleAnimator;
import com.mindecho.util.Encryptor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class MainController {
    
    @FXML
    private Label weatherLabel;
    
    @FXML
    private TextArea inputTextArea;
    
    @FXML
    private Button shredButton;
    
    @FXML
    private VBox responseBox;
    
    @FXML
    private Label responseText;
    
    @FXML
    private Label styleLabel;
    
    @FXML
    private StackPane animationContainer;
    
    private ShredParticleAnimator animator;
    
    @FXML
    private void initialize() {
        updateWeather();
        responseBox.setVisible(false);
        responseBox.managedProperty().bind(responseBox.visibleProperty());
        responseText.setWrapText(true);
        responseText.setMaxWidth(Double.MAX_VALUE);
        animator = new ShredParticleAnimator(400, 300);
        animator.setVisible(false);
        animationContainer.getChildren().add(animator);
    }
    
    private void updateWeather() {
        WeatherEngine weatherEngine = App.getServiceLocator().getWeatherEngine();
        LogStoreService logStore = App.getServiceLocator().getLogStoreService();
        
        List<com.mindecho.model.EmotionLabel> todayLabels = logStore.findByDate(java.time.LocalDate.now()).stream()
                .map(DestructionLog::getEmotionLabel)
                .collect(java.util.stream.Collectors.toList());
        
        EmotionWeather weather = weatherEngine.compute(todayLabels);
        weatherLabel.setText(weather.getIcon() + " " + weather.getDisplayName());
        
        try {
            com.mindecho.service.ResonanceAudioEngine audioEngine = App.getServiceLocator().getResonanceAudioEngine();
            if (audioEngine.isEnabled()) {
                audioEngine.switchTo(weather);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleShred() {
        String text = inputTextArea.getText().trim();
        if (text.isBlank()) {
            return;
        }

        shredButton.setDisable(true);
        responseBox.setVisible(false);
        String userText = text;
        inputTextArea.clear();

        showAnimation();
        App.getServiceLocator().getAiEngineService().generateResponseAsync(userText, response -> {
            handleResponse(userText, response);
        });
    }
    
    private void showAnimation() {
        animator.setVisible(true);
        animator.startAnimation(() -> {
            Platform.runLater(() -> {
                animator.setVisible(false);
            });
        });
    }
    
    private void handleResponse(String text, AiResponse response) {
        Platform.runLater(() -> {
            try {
                responseText.setText(response.responseText());
                styleLabel.setText(response.style().getDisplayName());
                styleLabel.getStyleClass().clear();
                styleLabel.getStyleClass().add("style-label");
                styleLabel.getStyleClass().add(response.style() == com.mindecho.model.AiStyle.GENTLE ? "gentle" : "sharp");
                responseBox.setVisible(true);

                persistLogSilently(text, response);
                updateWeatherByEmotion(response.emotion());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                shredButton.setDisable(false);
            }
        });
    }
    
    private void updateWeatherByEmotion(com.mindecho.model.EmotionLabel emotion) {
        EmotionWeather weather = switch (emotion) {
            case ANGER -> EmotionWeather.THUNDERSTORM;
            case ANXIETY -> EmotionWeather.CLOUDY;
            case SADNESS -> EmotionWeather.RAINY;
            case CALM -> EmotionWeather.SUNNY;
        };
        weatherLabel.setText(weather.getIcon() + " " + weather.getDisplayName());
        
        try {
            com.mindecho.service.ResonanceAudioEngine audioEngine = App.getServiceLocator().getResonanceAudioEngine();
            if (audioEngine.isEnabled()) {
                audioEngine.switchTo(weather);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void persistLogSilently(String text, AiResponse response) {
        try {
            Encryptor encryptor = App.getServiceLocator().getEncryptor();
            byte[] encrypted = encryptor.encrypt(text);

            DestructionLog log = new DestructionLog(
                    encrypted,
                    response.responseText(),
                    response.emotion(),
                    response.style(),
                    LocalDateTime.now()
            );

            App.getServiceLocator().getLogStoreService().save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
    @FXML
    private void goToSettings() throws IOException {
        App.setRoot("settings");
    }
}
