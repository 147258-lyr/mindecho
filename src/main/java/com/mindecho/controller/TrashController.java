package com.mindecho.controller;

import com.mindecho.App;
import com.mindecho.model.EmotionArticle;
import com.mindecho.model.EmotionWeather;
import com.mindecho.service.EmotionArticleService;
import com.mindecho.service.AiEngineService;
import com.mindecho.service.ResonanceAudioEngine;
import com.mindecho.util.EmotionClassifier;
import com.mindecho.model.EmotionLabel;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TrashController {

    @FXML private HBox tagButtons;
    @FXML private VBox articleList;
    @FXML private TextField moodInputField;
    @FXML private Label moodResultLabel;
    @FXML private Button moodAnalyzeButton;

    private EmotionArticleService articleService;
    private AiEngineService aiEngineService;
    private Button selectedTagButton;
    private final List<String> emotionOptions = Arrays.asList("焦虑", "压力", "悲伤", "愤怒", "快乐", "平静", "迷茫");

    @FXML
    private void initialize() {
        articleService = App.getServiceLocator().getEmotionArticleService();
        aiEngineService = App.getServiceLocator().getAiEngineService();
        setupTagButtons();
        loadArticles("全部");
    }

    private void setupTagButtons() {
        List<String> tags = articleService.getAllTags();
        
        Button allButton = new Button("全部");
        allButton.getStyleClass().add("tag-button");
        allButton.getStyleClass().add("tag-button-active");
        allButton.setOnAction(e -> handleTagClick("全部", allButton));
        tagButtons.getChildren().add(allButton);
        selectedTagButton = allButton;

        for (String tag : tags) {
            Button button = new Button(tag);
            button.getStyleClass().add("tag-button");
            button.setOnAction(e -> handleTagClick(tag, button));
            tagButtons.getChildren().add(button);
        }

        Button generateButton = new Button("✨ 生成文章");
        generateButton.getStyleClass().add("tag-button");
        generateButton.getStyleClass().add("tag-button-generate");
        generateButton.setOnAction(e -> showGenerateDialog());
        tagButtons.getChildren().add(generateButton);

        Button externalButton = new Button("🌐 网络文章");
        externalButton.getStyleClass().add("tag-button");
        externalButton.getStyleClass().add("tag-button-external");
        externalButton.setOnAction(e -> fetchExternalArticles());
        tagButtons.getChildren().add(externalButton);
    }

    private void handleTagClick(String tag, Button button) {
        if (selectedTagButton != null) {
            selectedTagButton.getStyleClass().remove("tag-button-active");
        }
        button.getStyleClass().add("tag-button-active");
        selectedTagButton = button;

        loadArticles(tag);
    }

    private void loadArticles(String tag) {
        articleList.getChildren().clear();
        
        List<EmotionArticle> articles;
        if ("全部".equals(tag)) {
            articles = articleService.getAllArticles();
        } else {
            articles = articleService.getArticlesByTag(tag);
        }

        for (EmotionArticle article : articles) {
            articleList.getChildren().add(createArticleCard(article));
        }
    }

    private VBox createArticleCard(EmotionArticle article) {
        VBox card = new VBox();
        card.getStyleClass().add("article-card");

        HBox header = new HBox();
        header.setSpacing(10);

        Label iconLabel = new Label(article.getIcon());
        iconLabel.getStyleClass().add("article-icon");

        VBox titleBox = new VBox();
        titleBox.setSpacing(4);

        Label titleLabel = new Label(article.getTitle());
        titleLabel.getStyleClass().add("article-title");

        Label summaryLabel = new Label(article.getSummary());
        summaryLabel.getStyleClass().add("article-summary");

        titleBox.getChildren().addAll(titleLabel, summaryLabel);

        Region spacer = new Region();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button readButton = new Button("阅读全文");
        readButton.getStyleClass().add("article-read-button");
        readButton.setOnAction(e -> openArticle(article));

        header.getChildren().addAll(iconLabel, titleBox, spacer, readButton);

        HBox tagsRow = new HBox();
        tagsRow.setSpacing(6);
        
        Label tagLabel = new Label(article.getTag());
        tagLabel.getStyleClass().add("article-tag");
        tagsRow.getChildren().add(tagLabel);

        for (String category : article.getCategories()) {
            Label categoryLabel = new Label(category);
            categoryLabel.getStyleClass().add("article-category");
            tagsRow.getChildren().add(categoryLabel);
        }

        card.getChildren().addAll(header, tagsRow);
        return card;
    }

    private void openArticle(EmotionArticle article) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("阅读文章 - " + article.getTitle());
            dialogStage.setWidth(600);
            dialogStage.setHeight(700);
            dialogStage.setResizable(false);

            VBox root = new VBox();
            root.getStyleClass().add("article-detail");
            root.setSpacing(16);
            root.setPadding(new Insets(24));

            Label iconLabel = new Label(article.getIcon());
            iconLabel.getStyleClass().add("article-detail-icon");

            Label titleLabel = new Label(article.getTitle());
            titleLabel.getStyleClass().add("article-detail-title");

            HBox metaRow = new HBox();
            metaRow.setSpacing(16);

            Label tagLabel = new Label("📁 " + article.getTag());
            tagLabel.getStyleClass().add("article-detail-meta");

            Label authorLabel = new Label("✍️ " + (article.getAuthor() != null ? article.getAuthor() : "未知"));
            authorLabel.getStyleClass().add("article-detail-meta");

            metaRow.getChildren().addAll(tagLabel, authorLabel);

            ScrollPane contentScrollPane = new ScrollPane();
            contentScrollPane.setFitToWidth(true);
            contentScrollPane.setFitToHeight(true);

            TextArea contentArea = new TextArea(article.getContent() != null ? article.getContent() : "");
            contentArea.getStyleClass().add("article-detail-content");
            contentArea.setEditable(false);
            contentArea.setWrapText(true);
            contentScrollPane.setContent(contentArea);

            Button closeButton = new Button("关闭");
            closeButton.getStyleClass().add("secondary-button");
            closeButton.setOnAction(e -> dialogStage.close());

            root.getChildren().addAll(iconLabel, titleLabel, metaRow, contentScrollPane, closeButton);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("/com/mindecho/css/" + App.getServiceLocator().getConfigManager().getTheme() + ".css").toExternalForm());
            dialogStage.setScene(scene);

            animateOpen(root);

            dialogStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("打开文章失败: " + e.getMessage());
        }
    }

    private void animateOpen(VBox root) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), root);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1);
        scale.setToY(1);

        FadeTransition fade = new FadeTransition(Duration.millis(400), root);
        fade.setFromValue(0.8);
        fade.setToValue(1);

        scale.play();
        fade.play();
    }

    private void showGenerateDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("✨ 生成情绪文章");
        dialogStage.setWidth(400);
        dialogStage.setHeight(400);
        dialogStage.setResizable(false);

        VBox root = new VBox();
        root.getStyleClass().add("article-detail");
        root.setSpacing(16);
        root.setPadding(new Insets(24));

        Label titleLabel = new Label("选择您想了解的情绪");
        titleLabel.getStyleClass().add("article-detail-title");

        HBox emotionGrid = new HBox();
        emotionGrid.setSpacing(8);
        emotionGrid.setPrefWidth(350);

        for (String emotion : emotionOptions) {
            Button emotionButton = new Button(emotion);
            emotionButton.getStyleClass().add("tag-button");
            emotionButton.setOnAction(e -> {
                dialogStage.close();
                generateArticle(emotion);
            });
            emotionGrid.getChildren().add(emotionButton);
        }

        Label customLabel = new Label("或输入自定义情绪：");
        customLabel.getStyleClass().add("article-detail-meta");

        TextField customField = new TextField();
        customField.setPromptText("例如：孤独、失落、不安...");
        customField.getStyleClass().add("text-input");

        Button customButton = new Button("生成");
        customButton.getStyleClass().add("primary-button");
        customButton.setOnAction(e -> {
            String emotion = customField.getText().trim();
            if (!emotion.isEmpty()) {
                dialogStage.close();
                generateArticle(emotion);
            }
        });

        root.getChildren().addAll(titleLabel, emotionGrid, customLabel, customField, customButton);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(App.class.getResource("/com/mindecho/css/" + App.getServiceLocator().getConfigManager().getTheme() + ".css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private void generateArticle(String emotion) {
        Stage loadingStage = new Stage();
        loadingStage.setTitle("✨ 正在生成文章");
        loadingStage.setWidth(300);
        loadingStage.setHeight(150);
        loadingStage.setResizable(false);

        VBox root = new VBox();
        root.setSpacing(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-alignment: center;");

        Label loadingLabel = new Label("正在为您创作关于「" + emotion + "」的文章...");
        loadingLabel.getStyleClass().add("article-detail-meta");

        root.getChildren().add(loadingLabel);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(App.class.getResource("/com/mindecho/css/" + App.getServiceLocator().getConfigManager().getTheme() + ".css").toExternalForm());
        loadingStage.setScene(scene);
        loadingStage.show();

        articleService.generateArticleByEmotion(emotion, article -> {
            loadingStage.close();
            loadArticles("全部");
            openArticle(article);
        });
    }

    private void fetchExternalArticles() {
        Stage loadingStage = new Stage();
        loadingStage.setTitle("🌐 正在获取网络文章");
        loadingStage.setWidth(300);
        loadingStage.setHeight(150);
        loadingStage.setResizable(false);

        VBox root = new VBox();
        root.setSpacing(16);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-alignment: center;");

        Label loadingLabel = new Label("正在从网络获取精选文章...");
        loadingLabel.getStyleClass().add("article-detail-meta");

        root.getChildren().add(loadingLabel);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(App.class.getResource("/com/mindecho/css/" + App.getServiceLocator().getConfigManager().getTheme() + ".css").toExternalForm());
        loadingStage.setScene(scene);
        loadingStage.show();

        articleService.fetchExternalArticles(3, articles -> {
            loadingStage.close();
            loadArticles("全部");
            if (!articles.isEmpty()) {
                openArticle(articles.get(0));
            }
        });
    }

    @FXML
    private void analyzeMood() {
        String text = moodInputField.getText().trim();
        if (text.isEmpty()) {
            moodResultLabel.setText("请先写下你的心情～");
            moodResultLabel.getStyleClass().removeAll("mood-result-success", "mood-result-analyzing", "mood-result-error");
            moodResultLabel.getStyleClass().add("mood-result-hint");
            return;
        }

        moodAnalyzeButton.setDisable(true);
        moodResultLabel.setText("✨ AI 正在分析你的心情...");
        moodResultLabel.getStyleClass().removeAll("mood-result-success", "mood-result-hint", "mood-result-error");
        moodResultLabel.getStyleClass().add("mood-result-analyzing");

        aiEngineService.analyzeEnergyAsync(text, result -> {
            moodAnalyzeButton.setDisable(false);
            
            if (result == null) {
                moodResultLabel.setText("😔 分析失败，请稍后再试");
                moodResultLabel.getStyleClass().removeAll("mood-result-analyzing", "mood-result-success", "mood-result-hint");
                moodResultLabel.getStyleClass().add("mood-result-error");
                return;
            }
            
            String emotion = result.emotion();
            if (emotion == null || emotion.isBlank()) {
                moodResultLabel.setText("😔 未能识别到你的情绪，请尝试描述得更详细一些");
                moodResultLabel.getStyleClass().removeAll("mood-result-analyzing", "mood-result-success", "mood-result-hint");
                moodResultLabel.getStyleClass().add("mood-result-error");
                return;
            }
            
            int stressLevel = result.stressLevel();
            
            String emotionDisplay = getEmotionDisplayName(emotion);
            String emotionTag = mapEmotionToTag(emotion);
            EmotionWeather weather = mapEmotionToWeather(emotion, stressLevel);
            
            moodResultLabel.setText("🎯 识别到你的情绪：" + emotionDisplay + " ｜ 压力等级：" + stressLevel + "/5 ｜ " + weather.getIcon() + " 已切换至「" + weather.getDisplayName() + "」氛围音 ｜ 已为你推荐「" + emotionTag + "」主题文章");
            moodResultLabel.getStyleClass().removeAll("mood-result-analyzing", "mood-result-hint", "mood-result-error");
            moodResultLabel.getStyleClass().add("mood-result-success");
            
            try {
                ResonanceAudioEngine audioEngine = App.getServiceLocator().getResonanceAudioEngine();
                if (audioEngine.isEnabled()) {
                    audioEngine.switchTo(weather);
                }
            } catch (Exception e) {
                System.err.println("切换音效失败: " + e.getMessage());
            }
            
            highlightTagButton(emotionTag);
            loadArticles(emotionTag);
        });
    }

    private String mapEmotionToTag(String emotion) {
        String normalized = emotion == null ? "" : emotion.trim().toUpperCase();
        return switch (normalized) {
            case "ANXIETY", "焦虑", "焦虑不安", "压力", "压力大" -> "焦虑管理";
            case "ANGER", "愤怒", "生气" -> "情绪管理";
            case "SADNESS", "悲伤", "难过" -> "悲伤疗愈";
            case "HAPPY", "快乐", "开心", "平静", "淡定", "CALM" -> "放松技巧";
            default -> "情绪管理";
        };
    }

    private EmotionWeather mapEmotionToWeather(String emotion, int stressLevel) {
        String normalized = emotion == null ? "" : emotion.trim().toUpperCase();
        return switch (normalized) {
            case "ANGER", "愤怒", "生气" -> EmotionWeather.THUNDERSTORM;
            case "ANXIETY", "焦虑", "焦虑不安", "压力", "压力大" -> stressLevel >= 4 ? EmotionWeather.THUNDERSTORM : EmotionWeather.CLOUDY;
            case "SADNESS", "悲伤", "难过" -> EmotionWeather.RAINY;
            case "HAPPY", "快乐", "开心", "平静", "淡定", "CALM" -> EmotionWeather.SUNNY;
            default -> EmotionWeather.SUNNY;
        };
    }

    private String getEmotionDisplayName(String emotion) {
        String normalized = emotion == null ? "" : emotion.trim().toUpperCase();
        return switch (normalized) {
            case "ANGER" -> "愤怒";
            case "ANXIETY" -> "焦虑";
            case "SADNESS" -> "悲伤";
            case "HAPPY", "CALM" -> "平静";
            default -> emotion == null ? "未知" : emotion;
        };
    }

    private void highlightTagButton(String tag) {
        if (selectedTagButton != null) {
            selectedTagButton.getStyleClass().remove("tag-button-active");
        }
        
        for (javafx.scene.Node node : tagButtons.getChildren()) {
            if (node instanceof Button button) {
                if (tag.equals(button.getText())) {
                    button.getStyleClass().add("tag-button-active");
                    selectedTagButton = button;
                    break;
                }
            }
        }
    }

    @FXML
    private void goToMain() throws IOException {
        App.setRoot("main");
    }

    @FXML
    private void goToScratch() throws IOException {
        App.setRoot("scratch");
    }

    @FXML
    private void goToReport() throws IOException {
        App.setRoot("report");
    }

    @FXML
    private void goToSettings() throws IOException {
        App.setRoot("settings");
    }
}