package com.mindecho.service;

import com.mindecho.model.AiResponse;
import java.util.function.Consumer;

public interface AiEngineService {
    record EnergyAnalysisResult(String emotion, int stressLevel, String responseText) {}

    void generateResponseAsync(String text, Consumer<AiResponse> callback);

    void analyzeEnergyAsync(String text, Consumer<EnergyAnalysisResult> callback);

    void generateGrowthReviewAsync(String content, String emotion, int daysPassed, Consumer<String> callback);

    void generateEmotionArticleAsync(String emotion, Consumer<String> callback);
}
