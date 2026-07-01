package com.mindecho.model;

public record AiResponse(
    String responseText,
    AiStyle style,
    EmotionLabel emotion
) {}
