package com.mindecho.service;

import com.mindecho.model.EmotionArticle;

import java.util.List;
import java.util.Optional;

public interface EmotionArticleService {
    List<EmotionArticle> getAllArticles();
    List<EmotionArticle> getArticlesByTag(String tag);
    List<EmotionArticle> getArticlesByCategory(String category);
    Optional<EmotionArticle> getArticleById(String id);
    List<String> getAllTags();
    List<String> getAllCategories();
    void generateArticleByEmotion(String emotion, java.util.function.Consumer<EmotionArticle> callback);
    void fetchExternalArticles(int count, java.util.function.Consumer<List<EmotionArticle>> callback);
    void fetchExternalArticleByCategory(String category, java.util.function.Consumer<EmotionArticle> callback);
}