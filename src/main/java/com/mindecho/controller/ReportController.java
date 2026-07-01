package com.mindecho.controller;

import com.mindecho.App;
import com.mindecho.model.EmotionLabel;
import com.mindecho.model.MonthlyReport;
import com.mindecho.service.ReportEngine;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportController {
    
    @FXML
    private Label monthLabel;
    
    @FXML
    private VBox emotionRankingBox;
    
    @FXML
    private VBox suggestionsBox;
    
    @FXML
    private VBox emptyState;
    
    @FXML
    private VBox aiSummaryBox;
    
    @FXML
    private Label aiSummaryLabel;
    
    @FXML
    private AnchorPane pieChartContainer;
    
    @FXML
    private AnchorPane stylePieChartContainer;
    
    @FXML
    private AnchorPane dailyChartContainer;
    
    @FXML
    private AnchorPane weeklyChartContainer;
    
    private static final Color ANGER_COLOR = Color.rgb(232, 90, 61);
    private static final Color ANXIETY_COLOR = Color.rgb(124, 102, 181);
    private static final Color SADNESS_COLOR = Color.rgb(107, 140, 255);
    private static final Color CALM_COLOR = Color.rgb(30, 156, 130);
    private static final Color GENTLE_COLOR = Color.rgb(53, 201, 176);
    private static final Color SHARP_COLOR = Color.rgb(255, 127, 125);
    
    @FXML
    private void initialize() {
        loadReport();
    }
    
    private void loadReport() {
        ReportEngine reportEngine = App.getServiceLocator().getReportEngine();
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        MonthlyReport report = reportEngine.generate(currentMonth);
        
        monthLabel.setText(currentMonth.toString() + " 情绪报告");
        
        if (report.emotionRanking().isEmpty()) {
            emptyState.setVisible(true);
            aiSummaryBox.setVisible(false);
            pieChartContainer.getParent().setVisible(false);
            stylePieChartContainer.getParent().getParent().setVisible(false);
            dailyChartContainer.getParent().setVisible(false);
            weeklyChartContainer.getParent().setVisible(false);
            emotionRankingBox.getParent().setVisible(false);
            suggestionsBox.getParent().setVisible(false);
            return;
        }
        
        emptyState.setVisible(false);
        aiSummaryBox.setVisible(true);
        pieChartContainer.getParent().setVisible(true);
        stylePieChartContainer.getParent().getParent().setVisible(true);
        dailyChartContainer.getParent().setVisible(true);
        weeklyChartContainer.getParent().setVisible(true);
        emotionRankingBox.getParent().setVisible(true);
        suggestionsBox.getParent().setVisible(true);
        
        renderAiSummary(report.aiSummary());
        renderEmotionPieChart(report.emotionDistribution());
        renderStylePieChart(report.gentleCount(), report.sharpCount());
·        renderWeeklyFrequencyChart(report.weeklyFrequency());
        renderEmotionRanking(report.emotionRanking());
        renderSuggestions(report.suggestions());
    }
    
    private void renderAiSummary(String summary) {
        if (summary == null || summary.isEmpty()) {
            aiSummaryBox.setVisible(false);
            return;
        }
        aiSummaryLabel.setText(summary);
        aiSummaryBox.setVisible(true);
    }
    
    private void renderEmotionPieChart(Map<EmotionLabel, Integer> distribution) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
        
        for (EmotionLabel label : EmotionLabel.values()) {
            int count = distribution.getOrDefault(label, 0);
            if (count > 0) {
                PieChart.Data data = new PieChart.Data(label.getDisplayName(), count);
                pieData.add(data);
            }
        }
        
        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(400, 220);
        
        pieChartContainer.getChildren().clear();
        pieChartContainer.getChildren().add(pieChart);
        AnchorPane.setTopAnchor(pieChart, 0.0);
        AnchorPane.setBottomAnchor(pieChart, 0.0);
        AnchorPane.setLeftAnchor(pieChart, 0.0);
        AnchorPane.setRightAnchor(pieChart, 0.0);
        
        String[] colors = {"#E85A3D", "#7C66B5", "#6b8cff", "#1E9C82", "#9E9E9E"};
        int index = 0;
        for (PieChart.Data data : pieData) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-pie-color: " + colors[Math.min(index, colors.length - 1)] + ";");
            }
            index++;
        }
    }
    
    private void renderStylePieChart(int gentleCount, int sharpCount) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        
        PieChart.Data gentleData = new PieChart.Data("温柔治愈", gentleCount);
        PieChart.Data sharpData = new PieChart.Data("清醒毒舌", sharpCount);
        
        pieData.addAll(gentleData, sharpData);
        
        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("");
        pieChart.setLabelsVisible(true);
        pieChart.setLegendVisible(true);
        pieChart.setPrefSize(400, 220);
        
        stylePieChartContainer.getChildren().clear();
        stylePieChartContainer.getChildren().add(pieChart);
        AnchorPane.setTopAnchor(pieChart, 0.0);
        AnchorPane.setBottomAnchor(pieChart, 0.0);
        AnchorPane.setLeftAnchor(pieChart, 0.0);
        AnchorPane.setRightAnchor(pieChart, 0.0);
        
        if (gentleData.getNode() != null) {
            gentleData.getNode().setStyle("-fx-pie-color: #35c9b0;");
        }
        if (sharpData.getNode() != null) {
            sharpData.getNode().setStyle("-fx-pie-color: #ff7f7d;");
        }
    }
    
    private void renderDailyEmotionChart(List<MonthlyReport.DailyEmotionData> dailyData) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("日期");
        yAxis.setLabel("次数");
        
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setPrefSize(800, 280);
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(true);
        
        XYChart.Series<String, Number> anger = new XYChart.Series<>();
        anger.setName("愤怒");
        
        XYChart.Series<String, Number> anxiety = new XYChart.Series<>();
        anxiety.setName("焦虑");
        
        XYChart.Series<String, Number> sadness = new XYChart.Series<>();
        sadness.setName("悲伤");
        
        XYChart.Series<String, Number> calm = new XYChart.Series<>();
        calm.setName("平静");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        int displayStep = Math.max(1, dailyData.size() / 10);
        for (int i = 0; i < dailyData.size(); i++) {
            MonthlyReport.DailyEmotionData data = dailyData.get(i);
            String dateStr = data.date().format(formatter);
            
            if (i % displayStep == 0 || i == dailyData.size() - 1) {
                anger.getData().add(new XYChart.Data<>(dateStr, data.angerCount()));
                anxiety.getData().add(new XYChart.Data<>(dateStr, data.anxietyCount()));
                sadness.getData().add(new XYChart.Data<>(dateStr, data.sadnessCount()));
                calm.getData().add(new XYChart.Data<>(dateStr, data.calmCount()));
            }
        }
        
        lineChart.getData().addAll(anger, anxiety, sadness, calm);
        
        dailyChartContainer.getChildren().clear();
        dailyChartContainer.getChildren().add(lineChart);
        AnchorPane.setTopAnchor(lineChart, 0.0);
        AnchorPane.setBottomAnchor(lineChart, 0.0);
        AnchorPane.setLeftAnchor(lineChart, 0.0);
        AnchorPane.setRightAnchor(lineChart, 0.0);
        
        if (anger.getNode() != null) anger.getNode().setStyle("-fx-stroke: #E85A3D; -fx-stroke-width: 2px;");
        if (anxiety.getNode() != null) anxiety.getNode().setStyle("-fx-stroke: #7C66B5; -fx-stroke-width: 2px;");
        if (sadness.getNode() != null) sadness.getNode().setStyle("-fx-stroke: #6b8cff; -fx-stroke-width: 2px;");
        if (calm.getNode() != null) calm.getNode().setStyle("-fx-stroke: #1E9C82; -fx-stroke-width: 2px;");
    }
    
    private void renderWeeklyFrequencyChart(List<Integer> weeklyFreq) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("周次");
        yAxis.setLabel("次数");
        
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("");
        barChart.setPrefSize(800, 200);
        barChart.setAnimated(true);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("倾诉次数");
        
        for (int i = 0; i < weeklyFreq.size(); i++) {
            series.getData().add(new XYChart.Data<>("第" + (i + 1) + "周", weeklyFreq.get(i)));
        }
        
        barChart.getData().add(series);
        
        weeklyChartContainer.getChildren().clear();
        weeklyChartContainer.getChildren().add(barChart);
        AnchorPane.setTopAnchor(barChart, 0.0);
        AnchorPane.setBottomAnchor(barChart, 0.0);
        AnchorPane.setLeftAnchor(barChart, 0.0);
        AnchorPane.setRightAnchor(barChart, 0.0);
        
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: #7C66B5;");
            }
        }
    }
    
    private void renderEmotionRanking(List<Map.Entry<EmotionLabel, Integer>> ranking) {
        emotionRankingBox.getChildren().clear();
        
        int rank = 1;
        for (Map.Entry<EmotionLabel, Integer> entry : ranking) {
            HBox itemBox = new HBox();
            itemBox.setSpacing(10);
            itemBox.getStyleClass().add("ranking-item-box");
            
            Label rankLabel = new Label(String.valueOf(rank));
            rankLabel.getStyleClass().add("ranking-number");
            
            Label emotionLabel = new Label(entry.getKey().getDisplayName());
            emotionLabel.getStyleClass().add("ranking-emotion");
            
            Label countLabel = new Label(entry.getValue() + "次");
            countLabel.getStyleClass().add("ranking-count");
            
            ProgressBar progressBar = new ProgressBar();
            int maxCount = ranking.get(0).getValue();
            progressBar.setProgress((double) entry.getValue() / maxCount);
            progressBar.getStyleClass().add("ranking-progress");
            
            itemBox.getChildren().addAll(rankLabel, emotionLabel, progressBar, countLabel);
            emotionRankingBox.getChildren().add(itemBox);
            rank++;
        }
    }
    
    private void renderSuggestions(List<String> suggestions) {
        suggestionsBox.getChildren().clear();
        
        for (String suggestion : suggestions) {
            Label label = new Label("💡 " + suggestion);
            label.getStyleClass().add("suggestion-item");
            label.setWrapText(true);
            suggestionsBox.getChildren().add(label);
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
    private void goToTrash() throws IOException {
        App.setRoot("trash");
    }
    
    @FXML
    private void goToSettings() throws IOException {
        App.setRoot("settings");
    }
}