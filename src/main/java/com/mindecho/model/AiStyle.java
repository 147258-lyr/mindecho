package com.mindecho.model;

public enum AiStyle {
    GENTLE("温柔治愈"),
    SHARP("清醒毒舌");

    private final String displayName;

    AiStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
