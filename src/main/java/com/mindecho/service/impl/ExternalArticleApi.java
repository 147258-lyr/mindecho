package com.mindecho.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExternalArticleApi {
    private static final String ZEN_QUOTES_API = "https://zenquotes.io/api/random";
    private static final String HITOKOTO_API = "https://v1.hitokoto.cn/?c=d&c=h&c=i&c=j&c=k&c=l";
    private static final String API_ZERO_HITOKOTO = "https://apizero.cn/marketplace/hitokoto";

    private final OkHttpClient client;
    private final Gson gson;
    private final Random random;

    public ExternalArticleApi() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.random = new Random();
    }

    public void fetchRandomArticlesAsync(int count, Consumer<List<ExternalArticleResult>> callback) {
        new Thread(() -> {
            List<ExternalArticleResult> results = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                try {
                    ExternalArticleResult result = fetchFromApi();
                    if (result != null && result.isValid()) {
                        results.add(result);
                    }
                    Thread.sleep(500);
                } catch (Exception e) {
                    // 忽略单个API调用失败，尝试下一个
                }
            }

            if (results.isEmpty()) {
                results.addAll(getFallbackArticles());
            }

            callback.accept(results);
        }).start();
    }

    public void fetchArticleByCategoryAsync(String category, Consumer<ExternalArticleResult> callback) {
        new Thread(() -> {
            ExternalArticleResult result = null;
            try {
                result = fetchFromApi();
                if (result != null && result.isValid()) {
                    result.setCategory(category);
                }
            } catch (Exception e) {
                // API调用失败，使用fallback
            }

            if (result == null || !result.isValid()) {
                result = getFallbackArticle(category);
            }

            callback.accept(result);
        }).start();
    }

    private ExternalArticleResult fetchFromApi() throws IOException {
        int apiChoice = random.nextInt(3);
        
        return switch (apiChoice) {
            case 0 -> fetchZenQuotes();
            case 1 -> fetchHitokoto();
            case 2 -> fetchApiZeroHitokoto();
            default -> null;
        };
    }

    private ExternalArticleResult fetchZenQuotes() throws IOException {
        Request request = new Request.Builder()
                .url(ZEN_QUOTES_API)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            JsonArray jsonArray = gson.fromJson(response.body().string(), JsonArray.class);
            if (jsonArray == null || jsonArray.isEmpty()) {
                return null;
            }

            JsonObject quote = jsonArray.get(0).getAsJsonObject();
            String text = quote.get("q").getAsString();
            String author = quote.get("a").getAsString();

            return new ExternalArticleResult(
                    "ZenQuotes",
                    "每日箴言",
                    text,
                    buildContentFromQuote(text),
                    "励志",
                    "📖",
                    author
            );
        }
    }

    private ExternalArticleResult fetchHitokoto() throws IOException {
        Request request = new Request.Builder()
                .url(HITOKOTO_API)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            String text = json.get("hitokoto").getAsString();
            String source = json.has("from") ? json.get("from").getAsString() : "网络";
            String category = getHitokotoCategory(json);

            return new ExternalArticleResult(
                    "Hitokoto",
                    "一言",
                    text,
                    buildContentFromQuote(text),
                    category,
                    "💫",
                    source
            );
        }
    }

    private ExternalArticleResult fetchApiZeroHitokoto() throws IOException {
        Request request = new Request.Builder()
                .url(API_ZERO_HITOKOTO)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            if (!json.has("code") || json.get("code").getAsInt() != 200) {
                return null;
            }

            JsonObject result = json.getAsJsonObject("result");
            if (result == null || !result.has("content")) {
                return null;
            }

            String text = result.get("content").getAsString();
            String author = result.has("author") ? result.get("author").getAsString() : "网络";
            String type = result.has("type") ? result.get("type").getAsString() : "励志";

            return new ExternalArticleResult(
                    "ApiZero",
                    "经典语录",
                    text,
                    buildContentFromQuote(text),
                    type,
                    "✨",
                    author
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String getHitokotoCategory(JsonObject json) {
        if (!json.has("c")) return "励志";
        
        String categoryCode = json.get("c").getAsString();
        return switch (categoryCode) {
            case "d" -> "励志";
            case "h" -> "哲理";
            case "i" -> "爱情";
            case "j" -> "诗词";
            case "k" -> "影视";
            case "l" -> "生活";
            default -> "励志";
        };
    }

    private String buildContentFromQuote(String quote) {
        return """
这段语录或许能给你带来一些启发：

"\" + quote + "\"

细细品味这句话，它蕴含着深刻的人生哲理。在忙碌的生活中，我们常常忽略了身边的美好。希望这句话能成为你今天的一盏明灯，照亮前行的道路。

记住，每一个平凡的日子都值得被珍惜，每一次小小的感悟都可能改变你的人生轨迹。
""";
    }

    private List<ExternalArticleResult> getFallbackArticles() {
        List<ExternalArticleResult> articles = new ArrayList<>();
        articles.add(new ExternalArticleResult(
                "本地", "生活感悟", "生活不是等待风暴过去，而是学会在雨中翩翩起舞。",
                "生活不是等待风暴过去，而是学会在雨中翩翩起舞。\n\n人生总会遇到各种困难和挑战，就像天气总会有阴晴雨雪。我们无法控制天气，但我们可以选择如何面对。\n\n与其抱怨雨水打湿了衣裳，不如学会在雨中跳舞。与其等待困难消失，不如学会在困难中成长。\n\n每一次挫折都是成长的机会，每一次跌倒都是为了更好地站起来。保持乐观的心态，你会发现生活中的美好无处不在。",
                "励志", "🌧️", "佚名"
        ));
        articles.add(new ExternalArticleResult(
                "本地", "心灵成长", "你所经历的一切，都是命运最好的安排。",
                "你所经历的一切，都是命运最好的安排。\n\n人生的每一段经历都有它的意义，无论是快乐还是痛苦，都是成长的一部分。\n\n那些让你痛苦的事情，教会了你坚强；那些让你快乐的事情，带给你力量。所有的经历共同塑造了今天的你。\n\n相信命运的安排，相信每一步都有它的道理。不要急于求成，也不要害怕挫折。时间会证明一切，你只需要耐心前行。",
                "哲理", "✨", "佚名"
        ));
        articles.add(new ExternalArticleResult(
                "本地", "情感智慧", "爱是理解，不是控制；是包容，不是改变。",
                "爱是理解，不是控制；是包容，不是改变。\n\n在人际关系中，我们常常希望对方按照自己的方式生活。但真正的爱，是尊重对方的独特性，理解对方的感受。\n\n不要试图改变一个人，而是学会接纳他本来的样子。不要试图控制一段关系，而是学会给予彼此空间。\n\n真正的爱，是两个人并肩前行，而不是一个人拖着另一个人走。学会理解和包容，你会发现爱的力量是无穷的。",
                "情感", "❤️", "佚名"
        ));
        return articles;
    }

    private ExternalArticleResult getFallbackArticle(String category) {
        return switch (category) {
            case "焦虑", "压力" -> new ExternalArticleResult(
                    "本地", "焦虑管理", "焦虑是成长的信号，不是失败的标志。",
                    "焦虑是成长的信号，不是失败的标志。\n\n感到焦虑，说明你对未来有所期待，对现状有所不满。这是成长的起点，不是崩溃的边缘。\n\n试着把焦虑转化为动力，把担忧转化为行动。每一次面对焦虑，都是一次锻炼内心力量的机会。\n\n深呼吸，告诉自己：我可以的。然后一步一步，慢慢前行。",
                    category, "🌈", "心灵导师"
            );
            case "悲伤", "失落" -> new ExternalArticleResult(
                    "本地", "悲伤疗愈", "悲伤是爱的证明，不是软弱的表现。",
                    "悲伤是爱的证明，不是软弱的表现。\n\n允许自己悲伤，意味着承认这份失去的重要性。哭泣不是羞耻的事，它是释放痛苦的方式。\n\n悲伤也是一种力量，它让我们更加珍惜现在所拥有的。给自己时间和空间去感受，去疗愈。",
                    category, "🌧️", "心灵导师"
            );
            default -> new ExternalArticleResult(
                    "本地", "每日箴言", "今天的努力，是明天的礼物。",
                    "今天的努力，是明天的礼物。\n\n不要小看每一天的积累，量变终将引起质变。坚持下去，你会看到意想不到的收获。",
                    category, "🌟", "心灵导师"
            );
        };
    }

    public static class ExternalArticleResult {
        private String source;
        private String title;
        private String summary;
        private String content;
        private String category;
        private String icon;
        private String author;

        public ExternalArticleResult(String source, String title, String summary, 
                                     String content, String category, String icon, String author) {
            this.source = source;
            this.title = title;
            this.summary = summary;
            this.content = content;
            this.category = category;
            this.icon = icon;
            this.author = author;
        }

        public boolean isValid() {
            return title != null && !title.isEmpty() && 
                   content != null && !content.isEmpty() && content.length() > 10;
        }

        public String getSource() { return source; }
        public String getTitle() { return title; }
        public String getSummary() { return summary; }
        public String getContent() { return content; }
        public String getCategory() { return category; }
        public String getIcon() { return icon; }
        public String getAuthor() { return author; }
        public void setCategory(String category) { this.category = category; }
    }
}
