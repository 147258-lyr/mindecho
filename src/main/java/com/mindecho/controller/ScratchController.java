package com.mindecho.controller;

import com.mindecho.App;
import com.mindecho.model.EmotionCapsule;
import com.mindecho.service.AiEngineService;
import com.mindecho.service.EmotionCapsuleService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ScratchController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private TextArea contentArea;
    @FXML private RadioButton radio3Days;
    @FXML private RadioButton radio7Days;
    @FXML private RadioButton radio30Days;
    @FXML private ToggleGroup openTimeGroup;
    @FXML private Button sealButton;
    @FXML private VBox emptyState;
    @FXML private VBox capsuleList;
    @FXML private ScrollPane contentScrollPane;

    private EmotionCapsuleService capsuleService;
    private AiEngineService aiEngineService;

    @FXML
    private void initialize() {
        capsuleService = App.getServiceLocator().getEmotionCapsuleService();
        aiEngineService = App.getServiceLocator().getAiEngineService();

        radio3Days.setToggleGroup(openTimeGroup);
        radio7Days.setToggleGroup(openTimeGroup);
        radio30Days.setToggleGroup(openTimeGroup);
        radio7Days.setSelected(true);

        emptyState.managedProperty().bind(emptyState.visibleProperty());
        capsuleList.managedProperty().bind(capsuleList.visibleProperty());

        loadCapsules();
    }

    private void loadCapsules() {
        capsuleList.getChildren().clear();
        List<EmotionCapsule> capsules = capsuleService.findAll();
        System.out.println("[DEBUG] loadCapsules: 找到 " + capsules.size() + " 个胶囊");
        System.out.println("[DEBUG] emptyState isVisible=" + emptyState.isVisible() + ", managed=" + emptyState.isManaged());
        System.out.println("[DEBUG] capsuleList isVisible=" + capsuleList.isVisible() + ", managed=" + capsuleList.isManaged());
        System.out.println("[DEBUG] capsuleList父节点: " + (capsuleList.getParent() != null ? capsuleList.getParent().getClass().getSimpleName() : "null"));

        if (capsules.isEmpty()) {
            emptyState.setVisible(true);
            capsuleList.setVisible(false);
            System.out.println("[DEBUG] 设置为：emptyState可见，capsuleList不可见");
            return;
        }

        emptyState.setVisible(false);
        capsuleList.setVisible(true);
        System.out.println("[DEBUG] 设置为：emptyState不可见，capsuleList可见");

        for (int i = 0; i < capsules.size(); i++) {
            EmotionCapsule capsule = capsules.get(i);
            boolean isNewest = (i == 0);
            System.out.println("[DEBUG] 加载胶囊: id=" + capsule.getId() + ", emotion=" + capsule.getEmotion() + ", createTime=" + capsule.getCreateTime() + ", isNewest=" + isNewest);
            VBox card = createCapsuleCard(capsule, isNewest);
            System.out.println("[DEBUG] 创建卡片: prefHeight=" + card.prefHeight(-1) + ", prefWidth=" + card.prefWidth(-1));
            capsuleList.getChildren().add(card);
        }
        
        System.out.println("[DEBUG] capsuleList子节点数: " + capsuleList.getChildren().size());
        
        Platform.runLater(() -> {
            System.out.println("[DEBUG] capsuleList布局后宽度: " + capsuleList.getWidth() + ", 高度: " + capsuleList.getHeight());
            System.out.println("[DEBUG] capsuleList布局边界: " + capsuleList.getBoundsInParent());
            System.out.println("[DEBUG] capsuleList prefHeight: " + capsuleList.prefHeight(-1));
            if (capsuleList.getParent() != null) {
                System.out.println("[DEBUG] 父容器宽度: " + capsuleList.getParent().getLayoutBounds().getWidth());
                System.out.println("[DEBUG] 父容器高度: " + capsuleList.getParent().getLayoutBounds().getHeight());
            }
        });
    }

    private VBox createCapsuleCard(EmotionCapsule capsule, boolean isNewest) {
        VBox card = new VBox();
        card.getStyleClass().add("capsule-card");
        card.setSpacing(10);
        card.setMinWidth(Region.USE_PREF_SIZE);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        card.setMinHeight(120);

        boolean unlocked = capsule.isUnlocked();

        HBox header = new HBox();
        header.setSpacing(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusIcon = new Label(unlocked ? "🔓" : "🔒");
        statusIcon.getStyleClass().add("capsule-icon");

        Label emotionLabel = new Label(getEmotionDisplayName(capsule.getEmotion()));
        emotionLabel.getStyleClass().add("capsule-emotion");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        if (isNewest) {
            Label newBadge = new Label("✨ 新");
            newBadge.setStyle("-fx-text-fill: #ff6b9d; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-color: rgba(255,107,157,0.1); -fx-padding: 2 8; -fx-background-radius: 10;");
            header.getChildren().addAll(statusIcon, emotionLabel, spacer, newBadge);
        } else {
            Label stressLabel = new Label("压力: " + capsule.getStressLevel() + "/5");
            stressLabel.getStyleClass().add("capsule-stress");
            header.getChildren().addAll(statusIcon, emotionLabel, spacer, stressLabel);
        }

        Label dateLabel = new Label("封存于 " + capsule.getCreateTime().format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("capsule-date");

        Label openTimeLabel = new Label();
        if (unlocked) {
            openTimeLabel.setText(capsule.isOpened() ? "已开启" : "可开启");
            openTimeLabel.getStyleClass().add("capsule-open-time-unlocked");
        } else {
            long remainingHours = ChronoUnit.HOURS.between(LocalDateTime.now(), capsule.getOpenTime());
            long remainingDays = remainingHours / 24;
            long remainingHoursLeft = remainingHours % 24;
            if (remainingDays > 0) {
                openTimeLabel.setText("还有 " + remainingDays + "天 " + remainingHoursLeft + "小时开启");
            } else {
                openTimeLabel.setText("还有 " + remainingHoursLeft + "小时开启");
            }
            openTimeLabel.getStyleClass().add("capsule-open-time-locked");
        }

        Button actionButton = new Button();
        if (unlocked) {
            actionButton.setText(capsule.isOpened() ? "查看详情" : "打开胶囊");
            actionButton.getStyleClass().add("capsule-action-button");
            actionButton.setOnAction(e -> openCapsule(capsule));
        } else {
            actionButton.setText("等待开启");
            actionButton.getStyleClass().add("capsule-action-button-disabled");
            actionButton.setDisable(true);
        }

        card.getChildren().addAll(header, dateLabel, openTimeLabel, actionButton);
        return card;
    }

    private String getEmotionDisplayName(String emotion) {
        return switch (emotion.toUpperCase()) {
            case "ANGER" -> "愤怒";
            case "ANXIETY" -> "焦虑";
            case "SADNESS" -> "悲伤";
            case "HAPPY" -> "愉悦";
            case "CALM" -> "平静";
            default -> emotion;
        };
    }

    @FXML
    private void sealCapsule() {
        String content = contentArea.getText().trim();
        if (content.isEmpty()) {
            showAlert("请先写下你想封存的内容");
            return;
        }

        sealButton.setDisable(true);
        sealButton.setText("分析中...");

        aiEngineService.analyzeEnergyAsync(content, result -> {
            int days = getSelectedDays();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime openTime = now.plusDays(days);

            EmotionCapsule capsule = new EmotionCapsule(
                    content,
                    result.emotion(),
                    result.stressLevel(),
                    extractKeywords(content),
                    result.responseText(),
                    now,
                    openTime
            );

            try {
                capsuleService.save(capsule);
                System.out.println("[DEBUG] sealCapsule: 保存成功，新胶囊ID=" + capsule.getId());

                Platform.runLater(() -> {
                    System.out.println("[DEBUG] sealCapsule: 开始更新UI");
                    animateSeal();
                    contentArea.clear();
                    sealButton.setDisable(false);
                    sealButton.setText("封存胶囊");
                    System.out.println("[DEBUG] sealCapsule: 调用loadCapsules");
                    loadCapsules();
                    System.out.println("[DEBUG] sealCapsule: 调用scrollToCapsuleList");
                    scrollToCapsuleList();
                    showAlert("胶囊已封存！将在 " + days + " 天后开启");
                    System.out.println("[DEBUG] sealCapsule: UI更新完成");
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    sealButton.setDisable(false);
                    sealButton.setText("封存胶囊");
                    showAlert("封存失败：" + e.getMessage());
                });
            }
        });
    }

    private int getSelectedDays() {
        Toggle selected = openTimeGroup.getSelectedToggle();
        if (selected == radio3Days) return 3;
        if (selected == radio30Days) return 30;
        return 7;
    }

    private String extractKeywords(String content) {
        if (content.length() <= 10) return content;
        return content.substring(0, 10) + "...";
    }

    private void animateSeal() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), sealButton);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(0.95);
        scale.setToY(0.95);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        RotateTransition rotate = new RotateTransition(Duration.millis(500), sealButton);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(1);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(0), e -> sealButton.setStyle("-fx-background-color: #2E8E7B;")),
                new KeyFrame(Duration.millis(300), e -> sealButton.setStyle("-fx-background-color: #1E9C82;"))
        );

        scale.play();
        rotate.play();
        timeline.play();
    }

    private void openCapsule(EmotionCapsule capsule) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(capsule.isOpened() ? "胶囊详情" : "打开胶囊");
        dialogStage.setWidth(500);
        dialogStage.setHeight(600);
        dialogStage.setResizable(false);

        VBox root = new VBox();
        root.getStyleClass().add("capsule-detail");
        root.setSpacing(16);
        root.setPadding(new Insets(24));

        Label titleLabel = new Label(capsule.isOpened() ? "📖 胶囊详情" : "💊 打开胶囊");
        titleLabel.getStyleClass().add("capsule-detail-title");

        if (!capsule.isOpened()) {
            Label unlockingLabel = new Label("正在打开胶囊...");
            unlockingLabel.getStyleClass().add("capsule-unlocking");
            root.getChildren().add(unlockingLabel);
        }

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox contentBox = new VBox();
        contentBox.setSpacing(12);
        scrollPane.setContent(contentBox);

        VBox originalContentSection = new VBox();
        originalContentSection.getStyleClass().add("capsule-section");
        Label contentTitle = new Label("📝 当时的烦恼");
        contentTitle.getStyleClass().add("capsule-section-title");
        TextArea contentText = new TextArea(capsule.getContent());
        contentText.getStyleClass().add("capsule-section-text");
        contentText.setEditable(false);
        contentText.setWrapText(true);
        originalContentSection.getChildren().addAll(contentTitle, contentText);

        VBox analysisSection = new VBox();
        analysisSection.getStyleClass().add("capsule-section");
        Label analysisTitle = new Label("🔍 AI情绪分析");
        analysisTitle.getStyleClass().add("capsule-section-title");

        HBox emotionRow = new HBox();
        emotionRow.setSpacing(16);
        emotionRow.getChildren().addAll(
                new Label("情绪：" + getEmotionDisplayName(capsule.getEmotion())),
                new Label("压力等级：" + capsule.getStressLevel() + "/5")
        );

        Label adviceLabel = new Label("💡 AI建议：" + capsule.getAiAdvice());
        adviceLabel.getStyleClass().add("capsule-advice");

        analysisSection.getChildren().addAll(analysisTitle, emotionRow, adviceLabel);

        contentBox.getChildren().addAll(originalContentSection, analysisSection);

        if (capsule.isOpened() && capsule.getReview() != null) {
            VBox reviewSection = new VBox();
            reviewSection.getStyleClass().add("capsule-section");
            Label reviewTitle = new Label("🌱 AI成长回顾");
            reviewTitle.getStyleClass().add("capsule-section-title");
            Label reviewText = new Label(capsule.getReview());
            reviewText.getStyleClass().add("capsule-review");
            reviewText.setWrapText(true);
            reviewSection.getChildren().addAll(reviewTitle, reviewText);
            contentBox.getChildren().add(reviewSection);
        }

        root.getChildren().addAll(titleLabel, scrollPane);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(App.class.getResource("/com/mindecho/css/" + App.getServiceLocator().getConfigManager().getTheme() + ".css").toExternalForm());
        dialogStage.setScene(scene);

        if (!capsule.isOpened()) {
            animateOpen(dialogStage, contentBox, capsule);
        }

        dialogStage.show();
    }

    private void animateOpen(Stage stage, VBox contentBox, EmotionCapsule capsule) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(800), contentBox);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(800), contentBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.setOnFinished(e -> generateReview(capsule, contentBox));
        parallel.play();
    }

    private void generateReview(EmotionCapsule capsule, VBox contentBox) {
        long daysPassed = ChronoUnit.DAYS.between(capsule.getCreateTime(), LocalDateTime.now());
        if (daysPassed < 1) daysPassed = 1;

        aiEngineService.generateGrowthReviewAsync(capsule.getContent(), capsule.getEmotion(), (int) daysPassed, review -> {
            capsule.setReview(review);
            capsule.setOpened(true);

            try {
                capsuleService.update(capsule);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                VBox reviewSection = new VBox();
                reviewSection.getStyleClass().add("capsule-section");
                reviewSection.setOpacity(0);

                Label reviewTitle = new Label("🌱 AI成长回顾");
                reviewTitle.getStyleClass().add("capsule-section-title");

                Label reviewText = new Label(review);
                reviewText.getStyleClass().add("capsule-review");
                reviewText.setWrapText(true);

                reviewSection.getChildren().addAll(reviewTitle, reviewText);
                contentBox.getChildren().add(reviewSection);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(600), reviewSection);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();

                loadCapsules();
            });
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void scrollToCapsuleList() {
        Platform.runLater(() -> {
            try {
                double contentHeight = contentScrollPane.getContent().getBoundsInLocal().getHeight();
                double viewportHeight = contentScrollPane.getViewportBounds().getHeight();
                double capsuleListY = capsuleList.getBoundsInParent().getMinY();
                
                double vvalue = Math.min(1.0, capsuleListY / (contentHeight - viewportHeight));
                System.out.println("[DEBUG] scrollToCapsuleList: vvalue=" + vvalue + ", capsuleListY=" + capsuleListY);
                
                contentScrollPane.setVvalue(vvalue);
            } catch (Exception e) {
                System.out.println("[DEBUG] scrollToCapsuleList异常: " + e.getMessage());
            }
        });
    }

    @FXML
    private void goToMain() throws IOException {
        App.setRoot("main");
    }

    @FXML
    private void goToReport() throws IOException {
        App.setRoot("report");
    }

    @FXML
    private void goToTrash() throws IOException {
        App.setRoot("trash");
    }

    @FXML
    private void goToSettings() throws IOException {
        App.setRoot("settings");
    }
}