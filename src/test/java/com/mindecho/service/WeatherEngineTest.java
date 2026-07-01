package com.mindecho.service;

import com.mindecho.model.EmotionLabel;
import com.mindecho.model.EmotionWeather;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherEngineTest {

    private final WeatherEngine weatherEngine = new WeatherEngine();

    @Test
    void shouldReturnSunnyWhenOnlyCalmRecordsExist() {
        assertEquals(EmotionWeather.SUNNY, weatherEngine.compute(List.of(EmotionLabel.CALM, EmotionLabel.CALM)));
    }

    @Test
    void shouldPrioritizeStormWhenAngerDominatesEmotionalRecords() {
        assertEquals(
                EmotionWeather.THUNDERSTORM,
                weatherEngine.compute(List.of(EmotionLabel.CALM, EmotionLabel.ANGER, EmotionLabel.ANGER, EmotionLabel.SADNESS))
        );
    }

    @Test
    void shouldReturnCloudyForAnxietyHeavyDay() {
        assertEquals(
                EmotionWeather.CLOUDY,
                weatherEngine.compute(List.of(EmotionLabel.CALM, EmotionLabel.ANXIETY, EmotionLabel.ANXIETY, EmotionLabel.SADNESS))
        );
    }

    @Test
    void shouldReturnRainyWhenSadnessOutweighsAnxiety() {
        assertEquals(
                EmotionWeather.RAINY,
                weatherEngine.compute(List.of(EmotionLabel.CALM, EmotionLabel.SADNESS, EmotionLabel.SADNESS, EmotionLabel.ANXIETY))
        );
    }
}
