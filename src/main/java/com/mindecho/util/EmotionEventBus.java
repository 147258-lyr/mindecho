package com.mindecho.util;

import com.mindecho.model.EmotionLabel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class EmotionEventBus {
    private static EmotionEventBus instance;
    private final ObjectProperty<EmotionLabel> latestEmotion;

    private EmotionEventBus() {
        latestEmotion = new SimpleObjectProperty<>();
    }

    public static synchronized EmotionEventBus getInstance() {
        if (instance == null) {
            instance = new EmotionEventBus();
        }
        return instance;
    }

    public void publish(EmotionLabel emotion) {
        latestEmotion.set(emotion);
    }

    public ObjectProperty<EmotionLabel> latestEmotionProperty() {
        return latestEmotion;
    }
}
