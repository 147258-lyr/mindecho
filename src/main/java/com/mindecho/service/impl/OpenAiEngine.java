package com.mindecho.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mindecho.model.AiResponse;
import com.mindecho.model.AiStyle;
import com.mindecho.model.EmotionLabel;
import com.mindecho.service.AiEngineService;
import com.mindecho.service.AiReplyPolicy;
import com.mindecho.util.ConfigManager;
import com.mindecho.util.EmotionClassifier;
import com.mindecho.util.FallbackPhraseStore;
import javafx.application.Platform;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OpenAiEngine implements AiEngineService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final List<String> HAPPY_KEYWORDS = List.of("开心", "快乐", "轻松", "兴奋", "高兴", "满足", "幸福");
    private static final List<String> HIGH_STRESS_KEYWORDS = List.of("崩溃", "受不了", "撑不住", "压得", "完蛋", "必须", "好累");

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private final ConfigManager configManager;
    private final Gson gson;
    private final Random random;
    private final FallbackPhraseStore fallbackPhraseStore;

    public OpenAiEngine(ConfigManager configManager) {
        this.configManager = configManager;
        this.gson = new Gson();
        this.random = new Random();
        this.fallbackPhraseStore = FallbackPhraseStore.getInstance();
    }

    @Override
    public void generateResponseAsync(String text, Consumer<AiResponse> callback) {
        new Thread(() -> {
            AiStyle style = random.nextBoolean() ? AiStyle.GENTLE : AiStyle.SHARP;
            EmotionLabel emotion = EmotionClassifier.classify(text);
            String responseText = fallbackPhraseStore.getRandom(style);

            try {
                if (configManager.isApiKeyConfigured()) {
                    responseText = callOpenAI(text, style);
                }
            } catch (Exception e) {
                responseText = fallbackPhraseStore.getRandom(style);
            }

            responseText = AiReplyPolicy.ensureLength(responseText, style, fallbackPhraseStore.getRandom(style));
            AiResponse response = new AiResponse(responseText, style, emotion);
            Platform.runLater(() -> callback.accept(response));
        }).start();
    }

    @Override
    public void analyzeEnergyAsync(String text, Consumer<EnergyAnalysisResult> callback) {
        new Thread(() -> {
            EnergyAnalysisResult result;
            try {
                result = configManager.isApiKeyConfigured()
                        ? callEnergyAnalysis(text)
                        : fallbackEnergyAnalysis(text);
            } catch (Exception e) {
                result = fallbackEnergyAnalysis(text);
            }

            EnergyAnalysisResult safeResult = new EnergyAnalysisResult(
                    sanitizeEmotion(result.emotion()),
                    clamp(result.stressLevel(), 1, 5),
                    AiReplyPolicy.ensureLength(
                            result.responseText(),
                            AiStyle.GENTLE,
                            "先看见这团情绪，再把它慢慢放出去。"
                    )
            );
            Platform.runLater(() -> callback.accept(safeResult));
        }).start();
    }

    @Override
    public void generateGrowthReviewAsync(String content, String emotion, int daysPassed, Consumer<String> callback) {
        new Thread(() -> {
            String review;
            try {
                review = configManager.isApiKeyConfigured()
                        ? callGrowthReview(content, emotion, daysPassed)
                        : fallbackGrowthReview(emotion, daysPassed);
            } catch (Exception e) {
                review = fallbackGrowthReview(emotion, daysPassed);
            }

            String safeReview = AiReplyPolicy.ensureLength(review, AiStyle.GENTLE, "时间会给你答案，你已经走过来了。");
            Platform.runLater(() -> callback.accept(safeReview));
        }).start();
    }

    private String callGrowthReview(String content, String emotion, int daysPassed) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", configManager.get("openai.model", "gpt-4o-mini"));
        requestBody.add("messages", gson.toJsonTree(List.of(
                Map.of("role", "system", "content",
                        "你是一位温暖的成长陪伴者。请根据用户过去记录的烦恼，结合已经过去的时间，" +
                        "生成一句真实、温暖、不鸡汤的成长总结（50字以内）。"),
                Map.of("role", "user", "content",
                        "用户曾经的烦恼：" + content + "\n" +
                        "情绪类型：" + emotion + "\n" +
                        "已经过去：" + daysPassed + "天\n" +
                        "请生成一句成长回顾总结。")
        )));
        requestBody.addProperty("max_tokens", Integer.parseInt(configManager.get("openai.max_tokens", "80")));

        JsonObject responseJson = executeChatCompletion(requestBody);
        return responseJson.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString()
                .trim();
    }

    private String fallbackGrowthReview(String emotion, int daysPassed) {
        if (daysPassed >= 30) {
            return "三十天前的烦恼，现在看来是不是轻了很多？";
        } else if (daysPassed >= 7) {
            return "一周过去了，你比当时更有力量了。";
        } else {
            return "三天的时间，足以让事情变得不同。";
        }
    }

    @Override
    public void generateEmotionArticleAsync(String emotion, Consumer<String> callback) {
        new Thread(() -> {
            String articleText;
            try {
                articleText = configManager.isApiKeyConfigured()
                        ? callEmotionArticle(emotion)
                        : fallbackEmotionArticle(emotion);
            } catch (Exception e) {
                articleText = fallbackEmotionArticle(emotion);
            }

            final String finalArticle = articleText;
            Platform.runLater(() -> callback.accept(finalArticle));
        }).start();
    }

    private String callEmotionArticle(String emotion) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", configManager.get("openai.model", "gpt-4o-mini"));
        requestBody.add("messages", gson.toJsonTree(List.of(
                Map.of("role", "system", "content",
                        "你是一位专业的心理咨询师和情感作家。请根据用户选择的情绪主题，" +
                        "写一篇温暖、治愈、实用的情绪释放文章。文章结构：标题 + 摘要 + 正文（分3-4个段落）。" +
                        "要求：语言温暖真诚，不鸡汤，有实际的建议和方法，适合阅读时长3-5分钟。"),
                Map.of("role", "user", "content",
                        "请围绕「" + emotion + "」这个情绪主题，写一篇情绪释放文章。" +
                        "输出格式要求：第一行为标题（用【】包裹），第二行为摘要，第三行开始为正文。")
        )));
        requestBody.addProperty("max_tokens", Integer.parseInt(configManager.get("openai.max_tokens", "600")));

        JsonObject responseJson = executeChatCompletion(requestBody);
        return responseJson.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString()
                .trim();
    }

    private String fallbackEmotionArticle(String emotion) {
        return switch (emotion) {
            case "焦虑" -> """
【与焦虑和解】
焦虑是成长的催化剂，学会与它共处。

焦虑是现代人最常见的情绪之一。我们常常被各种担忧困扰：工作、学习、健康、未来...焦虑像一只无形的手，紧紧抓住我们的心灵。

但焦虑也有它的积极面。它意味着你在乎，意味着你对未来有期待，意味着你想要变得更好。试着把焦虑看作成长的信号，而不是敌人。

与焦虑和解的第一步是接纳。告诉自己"我现在感到焦虑，这很正常"，不要与之对抗。第二步是转移注意力，做一些能让你专注的事情。第三步是寻求支持，与朋友或家人倾诉。

记住，你比焦虑更强大。每一次面对焦虑，都是一次锻炼内心力量的机会。
""";
            case "压力" -> """
【给自己留一片喘息空间】
在快节奏的生活中，学会暂停。

现代生活就像一条奔腾的河流，我们被推着向前走，很少有机会停下来看看周围的风景。工作、学习、家庭...各种压力像潮水般涌来。

但你知道吗？即便是奔腾的河流，也有平缓的地方。我们需要学会在压力中找到属于自己的喘息空间。

这个空间可以很小：每天早上起床后，给自己五分钟时间，什么都不做，只是静静地呼吸；或者在工作间隙，闭上眼睛，做几个深呼吸。

喘息空间不是浪费时间，而是为了更好地前行。试着每天给自己留出一段完全属于自己的时间，你会发现小小的喘息，能带来大大的改变。
""";
            case "悲伤" -> """
【悲伤是一种力量】
允许自己悲伤，是治愈的开始。

我们常常被教导要坚强，要微笑面对一切。但悲伤是人类正常的情感，它不是软弱的表现，而是对生活的深刻体验。

当你失去重要的人或事时，悲伤是自然的反应。压抑悲伤并不会让它消失，反而会让它在内心深处发酵，影响你的身心健康。

允许自己悲伤，意味着承认这份失去的重要性。哭泣不是羞耻的事，它是释放痛苦的方式。悲伤也是一种力量，它让我们更加珍惜现在所拥有的。

所以，不要害怕悲伤。给自己时间和空间去感受它，接纳它。当你学会与悲伤共处时，你会发现自己比想象中更有力量。
""";
            default -> """
【与情绪和平共处】
情绪不是敌人，而是内心的信使。

每个人都有情绪起伏的时候，焦虑、愤怒、悲伤...这些情绪常常被我们视为洪水猛兽。但实际上，情绪是我们内心最真实的声音。

每一种情绪背后，都藏着一个未被满足的需求。与情绪和平共处的第一步，是学会接纳。不要评判自己的情绪，告诉自己"我现在感到这样，这很正常"。

第二步是倾听。问问自己"这种情绪想要告诉我什么？"有时候答案很明显，有时候需要一点耐心。第三步是释放，找到适合自己的方式释放情绪。

记住，情绪没有对错，它只是一种信号。学会与它和平共处，你会发现内心的力量。
""";
        };
    }

    private String callOpenAI(String text, AiStyle style) throws IOException {
        String systemPrompt = AiReplyPolicy.buildSystemPrompt(style);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", configManager.get("openai.model", "gpt-4o-mini"));
        requestBody.add("messages", gson.toJsonTree(List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", text)
        )));
        requestBody.addProperty("max_tokens", Integer.parseInt(configManager.get("openai.max_tokens", "100")));

        String json = gson.toJson(requestBody);
        Request request = new Request.Builder()
                .url(configManager.get("openai.base.url", API_URL))
                .addHeader("Authorization", "Bearer " + configManager.get("openai.api.key"))
                .post(RequestBody.create(json, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }

            JsonObject responseJson = gson.fromJson(responseBody.string(), JsonObject.class);
            if (responseJson == null || !responseJson.has("choices")) {
                throw new IOException("Invalid response schema");
            }

            return responseJson.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        }
    }

    private EnergyAnalysisResult callEnergyAnalysis(String text) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", configManager.get("openai.model", "gpt-4o-mini"));
        requestBody.add("messages", gson.toJsonTree(List.of(
                Map.of("role", "system", "content",
                        "你是情绪能量分析器。请只输出JSON，不要附加解释。"
                                + "格式为 {\"emotion\":\"ANXIETY|ANGER|SADNESS|HAPPY\","
                                + "\"stressLevel\":1-5,\"responseText\":\"15到30字中文短句\"}"),
                Map.of("role", "user", "content", text)
        )));
        requestBody.addProperty("max_tokens", Integer.parseInt(configManager.get("openai.max_tokens", "100")));

        JsonObject responseJson = executeChatCompletion(requestBody);
        String content = responseJson.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        JsonObject parsed = gson.fromJson(extractJson(content), JsonObject.class);
        if (parsed == null) {
            throw new IOException("Invalid energy analysis payload");
        }

        if (!parsed.has("emotion") || parsed.get("emotion").isJsonNull()) {
            throw new IOException("Missing emotion field");
        }
        if (!parsed.has("stressLevel") || parsed.get("stressLevel").isJsonNull()) {
            throw new IOException("Missing stressLevel field");
        }

        String emotion = sanitizeEmotion(parsed.get("emotion").getAsString());
        int stressLevel = clamp(parsed.get("stressLevel").getAsInt(), 1, 5);
        String responseText = parsed.has("responseText") && !parsed.get("responseText").isJsonNull()
                ? parsed.get("responseText").getAsString()
                : "先看见这团情绪，再把它慢慢放出去。";
        return new EnergyAnalysisResult(emotion, stressLevel, responseText);
    }

    private JsonObject executeChatCompletion(JsonObject requestBody) throws IOException {
        String json = gson.toJson(requestBody);
        Request request = new Request.Builder()
                .url(configManager.get("openai.base.url", API_URL))
                .addHeader("Authorization", "Bearer " + configManager.get("openai.api.key"))
                .post(RequestBody.create(json, JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Empty response body");
            }

            JsonObject responseJson = gson.fromJson(responseBody.string(), JsonObject.class);
            if (responseJson == null || !responseJson.has("choices")) {
                throw new IOException("Invalid response schema");
            }
            return responseJson;
        }
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private EnergyAnalysisResult fallbackEnergyAnalysis(String text) {
        String emotion = detectEnergyEmotion(text);
        int stressLevel = computeStressLevel(text);
        return new EnergyAnalysisResult(emotion, stressLevel, fallbackEnergyText(emotion, stressLevel));
    }

    private String detectEnergyEmotion(String text) {
        String normalized = text == null ? "" : text.trim();
        String lowerText = normalized.toLowerCase();
        if (containsAny(lowerText, HAPPY_KEYWORDS)) {
            return "HAPPY";
        }

        EmotionLabel label = EmotionClassifier.classify(normalized);
        return switch (label) {
            case ANGER -> "ANGER";
            case ANXIETY -> "ANXIETY";
            case SADNESS -> "SADNESS";
            case CALM -> "HAPPY";
        };
    }

    private int computeStressLevel(String text) {
        String normalized = text == null ? "" : text.trim();
        int score = 1;

        if (normalized.length() > 30) {
            score++;
        }
        if (normalized.length() > 60) {
            score++;
        }
        if (normalized.contains("！！") || normalized.contains("??") || normalized.contains("？！") || normalized.contains("...")) {
            score++;
        }
        if (containsAny(normalized.toLowerCase(), HIGH_STRESS_KEYWORDS)) {
            score++;
        }

        long punctuationCount = normalized.chars()
                .filter(ch -> ch == '!' || ch == '！' || ch == '?' || ch == '？')
                .count();
        if (punctuationCount >= 3) {
            score++;
        }

        return clamp(score, 1, 5);
    }

    private String fallbackEnergyText(String emotion, int stressLevel) {
        return switch (emotion) {
            case "ANGER" -> stressLevel >= 4
                    ? "火焰正烧得很旺，先把最烫的部分释放出去。"
                    : "这团火还在翻涌，先稳住再慢慢放掉。";
            case "ANXIETY" -> stressLevel >= 4
                    ? "焦虑正在加速盘旋，先释放它再找回呼吸。"
                    : "这团不安仍在悬着，先让它慢慢降下来。";
            case "SADNESS" -> stressLevel >= 4
                    ? "悲伤已经积成重水，先把压抑释放出去。"
                    : "这滴情绪有些沉，轻一点放开会更舒服。";
            case "HAPPY" -> "光球里也装着紧绷，释放后会更轻盈。";
            default -> "先看见这团情绪，再把它慢慢放出去。";
        };
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String sanitizeEmotion(String emotion) {
        if (emotion == null) {
            return "HAPPY";
        }
        return switch (emotion.trim().toUpperCase()) {
            case "ANGER", "ANXIETY", "SADNESS", "HAPPY" -> emotion.trim().toUpperCase();
            default -> "HAPPY";
        };
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
