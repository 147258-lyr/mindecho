package com.mindecho.service.impl;

import com.mindecho.model.EmotionArticle;
import com.mindecho.service.AiEngineService;
import com.mindecho.service.EmotionArticleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultArticleService implements EmotionArticleService {
    private final List<EmotionArticle> articles;
    private final AiEngineService aiEngineService;
    private final ExternalArticleApi externalArticleApi;

    public DefaultArticleService(AiEngineService aiEngineService) {
        this.aiEngineService = aiEngineService;
        this.externalArticleApi = new ExternalArticleApi();
        this.articles = EmotionArticle.getDefaultArticles();
    }

    @Override
    public List<EmotionArticle> getAllArticles() {
        return new ArrayList<>(articles);
    }

    @Override
    public List<EmotionArticle> getArticlesByTag(String tag) {
        return articles.stream()
                .filter(a -> a.getTag().equalsIgnoreCase(tag))
                .collect(Collectors.toList());
    }

    @Override
    public List<EmotionArticle> getArticlesByCategory(String category) {
        return articles.stream()
                .filter(a -> a.getCategories().contains(category))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<EmotionArticle> getArticleById(String id) {
        return articles.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<String> getAllTags() {
        return articles.stream()
                .map(EmotionArticle::getTag)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        return articles.stream()
                .flatMap(a -> a.getCategories().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void generateArticleByEmotion(String emotion, java.util.function.Consumer<EmotionArticle> callback) {
        aiEngineService.generateEmotionArticleAsync(emotion, articleText -> {
            EmotionArticle article = parseArticle(articleText, emotion);
            articles.add(0, article);
            callback.accept(article);
        });
    }

    private EmotionArticle parseArticle(String articleText, String emotion) {
        String[] lines = articleText.split("\n");
        String title = "";
        String summary = "";
        StringBuilder content = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (i == 0 && line.startsWith("【") && line.endsWith("】")) {
                title = line.substring(1, line.length() - 1);
            } else if (i == 1) {
                summary = line;
            } else if (i > 1 && !line.isEmpty()) {
                if (content.length() > 0) {
                    content.append("\n\n");
                }
                content.append(line);
            }
        }

        if (title.isEmpty()) {
            title = emotion + " - 情绪释放";
        }
        if (summary.isEmpty()) {
            summary = "关于" + emotion + "的情绪释放文章";
        }
        if (content.length() == 0) {
            content.append(articleText);
        }

        String icon = getIconForEmotion(emotion);

        return new EmotionArticle(
                UUID.randomUUID().toString(),
                title,
                summary,
                content.toString(),
                emotion,
                icon,
                List.of("AI生成", emotion),
                "AI心灵导师"
        );
    }

    private String getIconForEmotion(String emotion) {
        return switch (emotion) {
            case "焦虑" -> "🌈";
            case "压力" -> "🌿";
            case "悲伤" -> "🌧️";
            case "愤怒" -> "🔥";
            case "快乐" -> "☀️";
            case "平静" -> "🌊";
            default -> "✨";
        };
    }

    @Override
    public void fetchExternalArticles(int count, java.util.function.Consumer<List<EmotionArticle>> callback) {
        externalArticleApi.fetchRandomArticlesAsync(count, results -> {
            List<EmotionArticle> emotionArticles = new ArrayList<>();
            for (ExternalArticleApi.ExternalArticleResult result : results) {
                EmotionArticle article = convertToEmotionArticle(result);
                emotionArticles.add(article);
                articles.add(0, article);
            }
            callback.accept(emotionArticles);
        });
    }

    @Override
    public void fetchExternalArticleByCategory(String category, java.util.function.Consumer<EmotionArticle> callback) {
        externalArticleApi.fetchArticleByCategoryAsync(category, result -> {
            EmotionArticle article = convertToEmotionArticle(result);
            articles.add(0, article);
            callback.accept(article);
        });
    }

    private EmotionArticle convertToEmotionArticle(ExternalArticleApi.ExternalArticleResult result) {
        return new EmotionArticle(
                UUID.randomUUID().toString(),
                result.getTitle(),
                result.getSummary(),
                result.getContent(),
                result.getCategory(),
                result.getIcon(),
                List.of("外部文章", result.getCategory()),
                result.getAuthor() + " (" + result.getSource() + ")"
        );
    }
}