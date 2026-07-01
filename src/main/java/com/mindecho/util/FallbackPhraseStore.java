package com.mindecho.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindecho.model.AiStyle;
import com.mindecho.service.AiReplyPolicy;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FallbackPhraseStore {
    private static FallbackPhraseStore instance;

    private Map<AiStyle, List<String>> phrases;

    private FallbackPhraseStore() {
        loadPhrases();
    }

    public static synchronized FallbackPhraseStore getInstance() {
        if (instance == null) {
            instance = new FallbackPhraseStore();
        }
        return instance;
    }

    private void loadPhrases() {
        try (InputStream is = getClass().getResourceAsStream("/fallback_phrases.json")) {
            if (is == null) {
                loadDefaultPhrases();
                return;
            }

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, List<String>>>() { }.getType();
            Map<String, List<String>> rawPhrases = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), type);

            phrases = new EnumMap<>(AiStyle.class);
            for (AiStyle style : AiStyle.values()) {
                List<String> validated = validatePhrases(rawPhrases.get(style.name()), style);
                phrases.put(style, validated.isEmpty() ? getDefaultPhrases(style) : validated);
            }
        } catch (Exception e) {
            loadDefaultPhrases();
        }
    }

    private void loadDefaultPhrases() {
        phrases = new EnumMap<>(AiStyle.class);
        for (AiStyle style : AiStyle.values()) {
            phrases.put(style, getDefaultPhrases(style));
        }
    }

    private List<String> validatePhrases(List<String> rawPhrases, AiStyle style) {
        if (rawPhrases == null || rawPhrases.isEmpty()) {
            return List.of();
        }

        List<String> validated = new ArrayList<>();
        for (String phrase : rawPhrases) {
            String normalized = AiReplyPolicy.normalize(phrase);
            if (AiReplyPolicy.isLengthValid(normalized)) {
                validated.add(normalized);
            }
        }
        return validated.isEmpty() ? getDefaultPhrases(style) : List.copyOf(validated);
    }

    private List<String> getDefaultPhrases(AiStyle style) {
        if (style == AiStyle.GENTLE) {
            return List.of(
                    "先让自己喘口气，你不必一个人扛完所有情绪。",
                    "你已经很努力了，今天先把自己安顿好就很好。",
                    "这份难受值得被看见，你可以慢一点也没关系。",
                    "别急着否定自己，你只是正在经历一段低潮。",
                    "先抱抱自己一下，情绪过去后路会慢慢清晰。"
            );
        }
        return List.of(
                "问题已经摆在眼前，先停下内耗再处理下一步。",
                "情绪可以有，但别让失控替你做长期决定。",
                "你现在最该做的，是收住慌张把重点拎出来。",
                "别反复自伤了，先解决眼前这一件更实际。",
                "难受归难受，先把能做的动作立刻执行起来。"
        );
    }

    public String getRandom(AiStyle style) {
        List<String> stylePhrases = phrases.getOrDefault(style, getDefaultPhrases(style));
        int index = ThreadLocalRandom.current().nextInt(stylePhrases.size());
        return stylePhrases.get(index);
    }

    public List<String> getAll(AiStyle style) {
        return Collections.unmodifiableList(phrases.getOrDefault(style, getDefaultPhrases(style)));
    }
}
