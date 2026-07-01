package com.mindecho.model;

public enum EmotionWeather {
    THUNDERSTORM("雷雨", "\u26A1"),
    CLOUDY("阴天", "\u2601"),
    RAINY("小雨", "\u2614"),
    SUNNY("晴天", "\u2600");

    private final String displayName;
    private final String icon;

    EmotionWeather(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
