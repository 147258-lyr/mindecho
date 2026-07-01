package com.mindecho.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public record MonthlyReport(
    YearMonth month,
    List<Map.Entry<EmotionLabel, Integer>> emotionRanking,
    List<Integer> weeklyFrequency,
    int gentleCount,
    int sharpCount,
    List<String> suggestions,
    List<DailyEmotionData> dailyEmotionData,
    Map<EmotionLabel, Integer> emotionDistribution,
    String aiSummary
) {
    public record DailyEmotionData(
        LocalDate date,
        int angerCount,
        int anxietyCount,
        int sadnessCount,
        int calmCount,
        int totalCount
    ) {}
}