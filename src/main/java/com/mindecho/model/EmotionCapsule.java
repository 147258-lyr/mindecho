package com.mindecho.model;

import java.time.LocalDateTime;

public class EmotionCapsule {
    private Long id;
    private String content;
    private String emotion;
    private int stressLevel;
    private String keywords;
    private String aiAdvice;
    private LocalDateTime createTime;
    private LocalDateTime openTime;
    private boolean opened;
    private String review;

    public EmotionCapsule() {}

    public EmotionCapsule(String content, String emotion, int stressLevel, String keywords,
                          String aiAdvice, LocalDateTime createTime, LocalDateTime openTime) {
        this.content = content;
        this.emotion = emotion;
        this.stressLevel = stressLevel;
        this.keywords = keywords;
        this.aiAdvice = aiAdvice;
        this.createTime = createTime;
        this.openTime = openTime;
        this.opened = false;
        this.review = null;
    }

    public boolean isUnlocked() {
        return opened || (openTime != null && LocalDateTime.now().isAfter(openTime));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }

    public int getStressLevel() { return stressLevel; }
    public void setStressLevel(int stressLevel) { this.stressLevel = stressLevel; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getAiAdvice() { return aiAdvice; }
    public void setAiAdvice(String aiAdvice) { this.aiAdvice = aiAdvice; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalDateTime openTime) { this.openTime = openTime; }

    public boolean isOpened() { return opened; }
    public void setOpened(boolean opened) { this.opened = opened; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
}