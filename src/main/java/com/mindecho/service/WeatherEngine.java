package com.mindecho.service;

import com.mindecho.model.EmotionLabel;
import com.mindecho.model.EmotionWeather;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeatherEngine {
    
    public EmotionWeather compute(List<EmotionLabel> todayLabels) {
        if (todayLabels.isEmpty()) {
            return EmotionWeather.SUNNY;
        }

        Map<EmotionLabel, Long> emotionCounts = todayLabels.stream()
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

        long angerCount = emotionCounts.getOrDefault(EmotionLabel.ANGER, 0L);
        long anxietyCount = emotionCounts.getOrDefault(EmotionLabel.ANXIETY, 0L);
        long sadnessCount = emotionCounts.getOrDefault(EmotionLabel.SADNESS, 0L);
        long calmCount = emotionCounts.getOrDefault(EmotionLabel.CALM, 0L);
        long totalCount = todayLabels.size();

        if (calmCount == totalCount) {
            return EmotionWeather.SUNNY;
        }

        long emotionalCount = angerCount + anxietyCount + sadnessCount;

        if (emotionalCount == 0) {
            return EmotionWeather.SUNNY;
        }

        double calmRatio = calmCount * 1.0 / totalCount;
        if (calmRatio >= 0.5) {
            return EmotionWeather.SUNNY;
        }

        if (angerCount * 1.0 / emotionalCount >= 0.4) {
            return EmotionWeather.THUNDERSTORM;
        }

        if (anxietyCount >= sadnessCount) {
            return EmotionWeather.CLOUDY;
        }

        return EmotionWeather.RAINY;
    }
}
