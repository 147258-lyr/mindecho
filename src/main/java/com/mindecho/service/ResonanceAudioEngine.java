package com.mindecho.service;

import com.mindecho.model.EmotionWeather;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.Map;

public class ResonanceAudioEngine {
    private static final Duration DEFAULT_TRANSITION = Duration.seconds(2);
    private static final double TARGET_VOLUME = 0.35;
    private static final Map<EmotionWeather, Soundscape> SOUND_MAP = Map.of(
            EmotionWeather.THUNDERSTORM, new Soundscape("/audio/anger.mp3", "低沉鼓点+压迫性风声"),
            EmotionWeather.CLOUDY, new Soundscape("/audio/anxiety.mp3", "低频心率+风声混合音景"),
            EmotionWeather.RAINY, new Soundscape("/audio/sadness.mp3", "雨声+空间混响"),
            EmotionWeather.SUNNY, new Soundscape("/audio/calm.mp3", "自然白噪音+轻风")
    );

    private final PlayerFactory playerFactory;
    private final VolumeFader volumeFader;

    private PlayerHandle currentPlayer;
    private EmotionWeather currentWeather;
    private boolean enabled;

    public ResonanceAudioEngine(boolean enabled) {
        this(enabled, new JavaFxPlayerFactory(), new TimelineVolumeFader(), DEFAULT_TRANSITION);
    }

    ResonanceAudioEngine(boolean enabled, PlayerFactory playerFactory, VolumeFader volumeFader, Duration transitionDuration) {
        this.enabled = enabled;
        this.playerFactory = playerFactory;
        this.volumeFader = volumeFader.withDuration(transitionDuration);
    }

    public synchronized void switchTo(EmotionWeather weather) {
        currentWeather = weather;
        if (!enabled || weather == null) {
            return;
        }

        if (currentPlayer != null && weather == currentPlayer.weather()) {
            return;
        }

        PlayerHandle nextPlayer = playerFactory.create(weather);
        if (nextPlayer == null) {
            return;
        }

        nextPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        nextPlayer.volumeProperty().set(0.0);
        nextPlayer.play();

        PlayerHandle previousPlayer = currentPlayer;
        currentPlayer = nextPlayer;

        if (previousPlayer != null) {
            fadeOutAndDispose(previousPlayer);
        }
        volumeFader.fade(nextPlayer, 0.0, TARGET_VOLUME, null);
    }

    public synchronized void stop() {
        if (currentPlayer != null) {
            fadeOutAndDispose(currentPlayer);
            currentPlayer = null;
        }
    }

    public synchronized void resume(EmotionWeather weather) {
        currentWeather = weather;
        if (enabled && weather != null) {
            switchTo(weather);
        }
    }

    public synchronized void setEnabled(boolean enabled) {
        boolean wasEnabled = this.enabled;
        this.enabled = enabled;
        if (!enabled) {
            stop();
        } else if (!wasEnabled && currentWeather != null) {
            switchTo(currentWeather);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    String getAudioPath(EmotionWeather weather) {
        Soundscape soundscape = SOUND_MAP.get(weather);
        return soundscape == null ? null : soundscape.resourcePath();
    }

    private void fadeOutAndDispose(PlayerHandle player) {
        double startVolume = player.volumeProperty().get();
        volumeFader.fade(player, startVolume, 0.0, () -> {
            player.stop();
            player.dispose();
        });
    }

    private record Soundscape(String resourcePath, String description) {
    }

    interface PlayerFactory {
        PlayerHandle create(EmotionWeather weather);
    }

    interface PlayerHandle {
        EmotionWeather weather();

        DoubleProperty volumeProperty();

        void setCycleCount(int cycleCount);

        void play();

        void stop();

        void dispose();
    }

    interface VolumeFader {
        void fade(PlayerHandle player, double fromVolume, double toVolume, Runnable onFinished);

        VolumeFader withDuration(Duration duration);
    }

    private static final class JavaFxPlayerFactory implements PlayerFactory {
        @Override
        public PlayerHandle create(EmotionWeather weather) {
            Soundscape soundscape = SOUND_MAP.get(weather);
            if (soundscape == null) {
                return null;
            }

            URL audioUrl = ResonanceAudioEngine.class.getResource(soundscape.resourcePath());
            if (audioUrl == null) {
                return null;
            }

            try {
                MediaPlayer mediaPlayer = new MediaPlayer(new Media(audioUrl.toExternalForm()));
                return new JavaFxPlayerHandle(weather, mediaPlayer);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static final class JavaFxPlayerHandle implements PlayerHandle {
        private final EmotionWeather weather;
        private final MediaPlayer mediaPlayer;

        private JavaFxPlayerHandle(EmotionWeather weather, MediaPlayer mediaPlayer) {
            this.weather = weather;
            this.mediaPlayer = mediaPlayer;
        }

        @Override
        public EmotionWeather weather() {
            return weather;
        }

        @Override
        public DoubleProperty volumeProperty() {
            return mediaPlayer.volumeProperty();
        }

        @Override
        public void setCycleCount(int cycleCount) {
            mediaPlayer.setCycleCount(cycleCount);
        }

        @Override
        public void play() {
            mediaPlayer.play();
        }

        @Override
        public void stop() {
            mediaPlayer.stop();
        }

        @Override
        public void dispose() {
            mediaPlayer.dispose();
        }
    }

    private static final class TimelineVolumeFader implements VolumeFader {
        private final Duration duration;

        private TimelineVolumeFader() {
            this(DEFAULT_TRANSITION);
        }

        private TimelineVolumeFader(Duration duration) {
            this.duration = duration;
        }

        @Override
        public void fade(PlayerHandle player, double fromVolume, double toVolume, Runnable onFinished) {
            Runnable runner = () -> {
                player.volumeProperty().set(fromVolume);
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(player.volumeProperty(), fromVolume)),
                        new KeyFrame(duration, event -> {
                            if (onFinished != null) {
                                onFinished.run();
                            }
                        }, new KeyValue(player.volumeProperty(), toVolume))
                );
                timeline.play();
            };

            if (Platform.isFxApplicationThread()) {
                runner.run();
            } else {
                Platform.runLater(runner);
            }
        }

        @Override
        public VolumeFader withDuration(Duration duration) {
            return new TimelineVolumeFader(duration);
        }
    }
}
