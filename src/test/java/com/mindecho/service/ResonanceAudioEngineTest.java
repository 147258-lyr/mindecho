package com.mindecho.service;

import com.mindecho.model.EmotionWeather;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResonanceAudioEngineTest {

    @Test
    void shouldExposeExpectedWeatherToAudioMappings() {
        FakePlayerFactory factory = new FakePlayerFactory();
        ImmediateFader fader = new ImmediateFader();
        ResonanceAudioEngine engine = new ResonanceAudioEngine(true, factory, fader, Duration.seconds(2));

        assertEquals("/audio/anger.mp3", engine.getAudioPath(EmotionWeather.THUNDERSTORM));
        assertEquals("/audio/anxiety.mp3", engine.getAudioPath(EmotionWeather.CLOUDY));
        assertEquals("/audio/sadness.mp3", engine.getAudioPath(EmotionWeather.RAINY));
        assertEquals("/audio/calm.mp3", engine.getAudioPath(EmotionWeather.SUNNY));
    }

    @Test
    void shouldFadeBetweenTwoWeatherTracks() {
        FakePlayerFactory factory = new FakePlayerFactory();
        ImmediateFader fader = new ImmediateFader();
        ResonanceAudioEngine engine = new ResonanceAudioEngine(true, factory, fader, Duration.seconds(2));

        engine.switchTo(EmotionWeather.SUNNY);
        FakePlayer sunnyPlayer = factory.createdPlayers.get(0);
        assertTrue(sunnyPlayer.played);
        assertEquals(MediaPlayer.INDEFINITE, sunnyPlayer.cycleCount);
        assertEquals(0.35, sunnyPlayer.volumeProperty().get(), 0.0001);

        engine.switchTo(EmotionWeather.RAINY);
        FakePlayer rainyPlayer = factory.createdPlayers.get(1);
        assertTrue(sunnyPlayer.stopped);
        assertTrue(sunnyPlayer.disposed);
        assertTrue(rainyPlayer.played);
        assertEquals(0.35, rainyPlayer.volumeProperty().get(), 0.0001);
        assertEquals(2.0, fader.duration.toSeconds(), 0.0001);
    }

    @Test
    void shouldResumeCurrentWeatherWhenAudioReEnabled() {
        FakePlayerFactory factory = new FakePlayerFactory();
        ImmediateFader fader = new ImmediateFader();
        ResonanceAudioEngine engine = new ResonanceAudioEngine(false, factory, fader, Duration.seconds(2));

        engine.switchTo(EmotionWeather.CLOUDY);
        assertTrue(factory.createdPlayers.isEmpty());

        engine.setEnabled(true);
        assertEquals(1, factory.createdPlayers.size());
        assertEquals(EmotionWeather.CLOUDY, factory.createdPlayers.get(0).weather());
    }

    private static final class FakePlayerFactory implements ResonanceAudioEngine.PlayerFactory {
        private final List<FakePlayer> createdPlayers = new ArrayList<>();

        @Override
        public ResonanceAudioEngine.PlayerHandle create(EmotionWeather weather) {
            FakePlayer player = new FakePlayer(weather);
            createdPlayers.add(player);
            return player;
        }
    }

    private static final class FakePlayer implements ResonanceAudioEngine.PlayerHandle {
        private final EmotionWeather weather;
        private final DoubleProperty volume = new SimpleDoubleProperty(0.0);
        private boolean played;
        private boolean stopped;
        private boolean disposed;
        private int cycleCount;

        private FakePlayer(EmotionWeather weather) {
            this.weather = weather;
        }

        @Override
        public EmotionWeather weather() {
            return weather;
        }

        @Override
        public DoubleProperty volumeProperty() {
            return volume;
        }

        @Override
        public void setCycleCount(int cycleCount) {
            this.cycleCount = cycleCount;
        }

        @Override
        public void play() {
            this.played = true;
        }

        @Override
        public void stop() {
            this.stopped = true;
        }

        @Override
        public void dispose() {
            this.disposed = true;
        }
    }

    private static final class ImmediateFader implements ResonanceAudioEngine.VolumeFader {
        private final Duration duration;

        private ImmediateFader() {
            this(Duration.seconds(2));
        }

        private ImmediateFader(Duration duration) {
            this.duration = duration;
        }

        @Override
        public void fade(ResonanceAudioEngine.PlayerHandle player, double fromVolume, double toVolume, Runnable onFinished) {
            player.volumeProperty().set(fromVolume);
            player.volumeProperty().set(toVolume);
            if (onFinished != null) {
                onFinished.run();
            }
        }

        @Override
        public ResonanceAudioEngine.VolumeFader withDuration(Duration duration) {
            return new ImmediateFader(duration);
        }
    }
}
