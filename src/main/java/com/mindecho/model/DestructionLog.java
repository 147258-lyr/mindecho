package com.mindecho.model;

import java.time.LocalDateTime;

public class DestructionLog {
    private Long id;
    private byte[] encryptedText;
    private String aiResponse;
    private EmotionLabel emotionLabel;
    private AiStyle aiStyle;
    private LocalDateTime createdAt;

    public DestructionLog() {}

    public DestructionLog(byte[] encryptedText, String aiResponse, EmotionLabel emotionLabel, AiStyle aiStyle, LocalDateTime createdAt) {
        this.encryptedText = encryptedText;
        this.aiResponse = aiResponse;
        this.emotionLabel = emotionLabel;
        this.aiStyle = aiStyle;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getEncryptedText() {
        return encryptedText;
    }

    public void setEncryptedText(byte[] encryptedText) {
        this.encryptedText = encryptedText;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public EmotionLabel getEmotionLabel() {
        return emotionLabel;
    }

    public void setEmotionLabel(EmotionLabel emotionLabel) {
        this.emotionLabel = emotionLabel;
    }

    public AiStyle getAiStyle() {
        return aiStyle;
    }

    public void setAiStyle(AiStyle aiStyle) {
        this.aiStyle = aiStyle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
