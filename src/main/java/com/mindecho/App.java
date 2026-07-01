package com.mindecho;

import com.mindecho.model.EmotionLabel;
import com.mindecho.model.EmotionWeather;
import com.mindecho.service.LogStoreService;
import com.mindecho.service.ResonanceAudioEngine;
import com.mindecho.service.WeatherEngine;
import com.mindecho.util.DatabaseHelper;
import com.mindecho.util.EmotionEventBus;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    private static Scene scene;
    private static ServiceLocator serviceLocator;
    
    @Override
    public void init() {
        serviceLocator = ServiceLocator.getInstance();
        DatabaseHelper.initDatabase();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // 先初始化数据库
        try {
            DatabaseHelper.initDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        scene = new Scene(loadFXML("main"), 980, 760);
        
        applyTheme();
        setupWeatherAudio();
        
        stage.setTitle("MindEcho - 智愈心海");
        stage.setScene(scene);
        stage.setMinWidth(860);
        stage.setMinHeight(640);
        stage.show();
    }
    
    public static void applyTheme() {
        String theme = serviceLocator.getConfigManager().getTheme();
        updateThemeStylesheet(theme);
    }
    
    private void setupWeatherAudio() {
        WeatherEngine weatherEngine = serviceLocator.getWeatherEngine();
        LogStoreService logStore = serviceLocator.getLogStoreService();
        ResonanceAudioEngine audioEngine = serviceLocator.getResonanceAudioEngine();
        EmotionEventBus eventBus = serviceLocator.getEmotionEventBus();
        
        List<EmotionLabel> todayLabels = new ArrayList<>();
        try {
            todayLabels = logStore.findByDate(java.time.LocalDate.now()).stream()
                    .map(log -> log.getEmotionLabel())
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        EmotionWeather initialWeather = weatherEngine.compute(todayLabels);
        audioEngine.switchTo(initialWeather);
        
        eventBus.latestEmotionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    List<EmotionLabel> newTodayLabels = logStore.findByDate(java.time.LocalDate.now()).stream()
                            .map(log -> log.getEmotionLabel())
                            .collect(java.util.stream.Collectors.toList());
                    EmotionWeather newWeather = weatherEngine.compute(newTodayLabels);
                    audioEngine.switchTo(newWeather);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        applyTheme();
    }

    public static void updateThemeStylesheet(String theme) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(App.class.getResource("/com/mindecho/css/" + theme + ".css").toExternalForm());
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/mindecho/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public static void main(String[] args) {
        launch();
    }
}
