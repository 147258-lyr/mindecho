package com.mindecho.service;

import com.mindecho.model.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

public class ReportEngine {
    private final LogStoreService logStore;
    private final AiEngineService aiEngine;

    public ReportEngine(LogStoreService logStore, AiEngineService aiEngine) {
        this.logStore = logStore;
        this.aiEngine = aiEngine;
    }

    public MonthlyReport generate(YearMonth month) {
        List<DestructionLog> logs = logStore.findByMonth(month);
        
        if (logs.isEmpty()) {
            return new MonthlyReport(month, Collections.emptyList(), Collections.emptyList(), 0, 0, 
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), "");
        }

        List<Map.Entry<EmotionLabel, Integer>> emotionRanking = calculateEmotionRanking(logs);
        List<Integer> weeklyFrequency = calculateWeeklyFrequency(logs, month);
        Map<AiStyle, Integer> styleCounts = calculateStyleCounts(logs);
        List<String> suggestions = generateSuggestions(emotionRanking);
        List<MonthlyReport.DailyEmotionData> dailyEmotionData = calculateDailyEmotionData(logs, month);
        Map<EmotionLabel, Integer> emotionDistribution = calculateEmotionDistribution(logs);
        String aiSummary = generateAiSummary(emotionRanking, weeklyFrequency, styleCounts);

        return new MonthlyReport(
                month,
                emotionRanking,
                weeklyFrequency,
                styleCounts.getOrDefault(AiStyle.GENTLE, 0),
                styleCounts.getOrDefault(AiStyle.SHARP, 0),
                suggestions,
                dailyEmotionData,
                emotionDistribution,
                aiSummary
        );
    }

    private List<Map.Entry<EmotionLabel, Integer>> calculateEmotionRanking(List<DestructionLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(DestructionLog::getEmotionLabel, Collectors.summingInt(e -> 1)))
                .entrySet().stream()
                .sorted(Map.Entry.<EmotionLabel, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    private List<Integer> calculateWeeklyFrequency(List<DestructionLog> logs, YearMonth month) {
        int weeksInMonth = month.atEndOfMonth().get(ChronoField.ALIGNED_WEEK_OF_MONTH);
        List<Integer> weeklyCounts = new ArrayList<>(Collections.nCopies(weeksInMonth, 0));

        for (DestructionLog log : logs) {
            LocalDate date = log.getCreatedAt().toLocalDate();
            int weekOfMonth = date.get(ChronoField.ALIGNED_WEEK_OF_MONTH) - 1;
            if (weekOfMonth >= 0 && weekOfMonth < weeksInMonth) {
                weeklyCounts.set(weekOfMonth, weeklyCounts.get(weekOfMonth) + 1);
            }
        }

        return weeklyCounts;
    }

    private Map<AiStyle, Integer> calculateStyleCounts(List<DestructionLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(DestructionLog::getAiStyle, Collectors.summingInt(e -> 1)));
    }

    private List<MonthlyReport.DailyEmotionData> calculateDailyEmotionData(List<DestructionLog> logs, YearMonth month) {
        Map<LocalDate, int[]> dailyCounts = new TreeMap<>();
        
        int daysInMonth = month.lengthOfMonth();
        LocalDate firstDay = month.atDay(1);
        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = firstDay.plusDays(i);
            dailyCounts.put(date, new int[5]);
        }

        for (DestructionLog log : logs) {
            LocalDate date = log.getCreatedAt().toLocalDate();
            int[] counts = dailyCounts.get(date);
            if (counts != null) {
                EmotionLabel emotion = log.getEmotionLabel();
                switch (emotion) {
                    case ANGER -> counts[0]++;
                    case ANXIETY -> counts[1]++;
                    case SADNESS -> counts[2]++;
                    case CALM -> counts[3]++;
                }
                counts[4]++;
            }
        }

        return dailyCounts.entrySet().stream()
                .map(entry -> new MonthlyReport.DailyEmotionData(
                        entry.getKey(),
                        entry.getValue()[0],
                        entry.getValue()[1],
                        entry.getValue()[2],
                        entry.getValue()[3],
                        entry.getValue()[4]
                ))
                .collect(Collectors.toList());
    }

    private Map<EmotionLabel, Integer> calculateEmotionDistribution(List<DestructionLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(DestructionLog::getEmotionLabel, Collectors.summingInt(e -> 1)));
    }

    private String generateAiSummary(List<Map.Entry<EmotionLabel, Integer>> emotionRanking,
                                     List<Integer> weeklyFrequency,
                                     Map<AiStyle, Integer> styleCounts) {
        if (emotionRanking.isEmpty()) {
            return "";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("请帮我分析以下月度情绪数据，用简洁温暖的语言生成一段月度总结（100-150字）：\n");
        prompt.append("情绪分布：");
        for (Map.Entry<EmotionLabel, Integer> entry : emotionRanking) {
            prompt.append(entry.getKey().getDisplayName()).append(entry.getValue()).append("次，");
        }
        prompt.append("\n倾诉频率：本周共").append(weeklyFrequency.stream().mapToInt(Integer::intValue).sum())
                .append("次");
        prompt.append("\nAI回应风格：温柔治愈").append(styleCounts.getOrDefault(AiStyle.GENTLE, 0))
                .append("次，清醒毒舌").append(styleCounts.getOrDefault(AiStyle.SHARP, 0)).append("次");
        prompt.append("\n请给出一句温暖的总结和鼓励。");

        final String[] summary = {""};
        aiEngine.generateResponseAsync(prompt.toString(), response -> {
            if (response != null && response.responseText() != null) {
                summary[0] = response.responseText();
            }
        });

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return summary[0].isEmpty() ? generateFallbackSummary(emotionRanking, weeklyFrequency) : summary[0];
    }

    private String generateFallbackSummary(List<Map.Entry<EmotionLabel, Integer>> emotionRanking,
                                           List<Integer> weeklyFrequency) {
        EmotionLabel topEmotion = emotionRanking.get(0).getKey();
        int totalCount = emotionRanking.stream().mapToInt(Map.Entry::getValue).sum();

        String emotionDesc;
        String advice;
        switch (topEmotion) {
            case ANGER -> {
                emotionDesc = "这个月你经历了不少愤怒";
                advice = "试着深呼吸，给情绪一个出口";
            }
            case ANXIETY -> {
                emotionDesc = "这个月你有些焦虑";
                advice = "慢慢来，每一步都算数";
            }
            case SADNESS -> {
                emotionDesc = "这个月你感到了一些悲伤";
                advice = "允许自己难过，明天会更好";
            }
            case CALM -> {
                emotionDesc = "这个月你保持着平静的心态";
                advice = "继续保持，你做得很棒";
            }
            default -> {
                emotionDesc = "这个月你的情绪很丰富";
                advice = "继续记录，倾听内心的声音";
            }
        }

        return String.format("%s，共倾诉了 %d 次。%s。感谢你选择记录自己的心情，每一次倾诉都是对自己的关爱。",
                emotionDesc, totalCount, advice);
    }

    private List<String> generateSuggestions(List<Map.Entry<EmotionLabel, Integer>> emotionRanking) {
        List<String> suggestions = new ArrayList<>();
        
        if (emotionRanking.isEmpty()) {
            return suggestions;
        }

        EmotionLabel topEmotion = emotionRanking.get(0).getKey();
        
        switch (topEmotion) {
            case ANGER:
                suggestions.add("建议尝试深呼吸，每次5-10秒。");
                suggestions.add("可以写下来让你生气的事，然后撕掉它。");
                break;
            case ANXIETY:
                suggestions.add("试着做5-10分钟的冥想。");
                suggestions.add("出去走走，呼吸新鲜空气。");
                break;
            case SADNESS:
                suggestions.add("允许自己难过，这是正常的。");
                suggestions.add("和信任的人聊聊天。");
                break;
            case CALM:
                suggestions.add("保持现在的状态，你做得很好！");
                suggestions.add("可以记录一下现在的心情。");
                break;
        }

        return suggestions;
    }
}