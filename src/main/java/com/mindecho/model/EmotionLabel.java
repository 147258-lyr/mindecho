package com.mindecho.model;

public enum EmotionLabel {
    ANGER("愤怒"),
    ANXIETY("焦虑"),
    SADNESS("悲伤"),
    CALM("平静");

    private final String displayName;

    EmotionLabel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
