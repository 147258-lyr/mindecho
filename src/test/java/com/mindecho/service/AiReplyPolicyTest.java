package com.mindecho.service;

import com.mindecho.model.AiStyle;
import com.mindecho.util.FallbackPhraseStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiReplyPolicyTest {

    @Test
    void shouldExposeDistinctPromptsForTwoStyles() {
        String gentlePrompt = AiReplyPolicy.buildSystemPrompt(AiStyle.GENTLE);
        String sharpPrompt = AiReplyPolicy.buildSystemPrompt(AiStyle.SHARP);

        assertNotEquals(gentlePrompt, sharpPrompt);
        assertTrue(gentlePrompt.contains("温和"));
        assertTrue(gentlePrompt.contains("委婉"));
        assertTrue(sharpPrompt.contains("直接"));
        assertTrue(sharpPrompt.contains("凝练"));
    }

    @Test
    void shouldValidateAllFallbackPhrasesLength() {
        FallbackPhraseStore store = FallbackPhraseStore.getInstance();

        for (AiStyle style : AiStyle.values()) {
            for (String phrase : store.getAll(style)) {
                assertTrue(
                        AiReplyPolicy.isLengthValid(phrase),
                        () -> style + " 存在不合规回复: " + phrase + "，长度=" + AiReplyPolicy.normalize(phrase).length()
                );
            }
        }
    }

    @Test
    void shouldFallbackWhenResponseLengthIsInvalid() {
        FallbackPhraseStore store = FallbackPhraseStore.getInstance();

        String normalized = AiReplyPolicy.ensureLength("太短", AiStyle.GENTLE, store.getRandom(AiStyle.GENTLE));
        assertTrue(AiReplyPolicy.isLengthValid(normalized));

        String tooLong = "这是一段明显超过四十个中文字符的回复文本用于验证超长内容会被系统自动拦截并替换";
        String normalizedLong = AiReplyPolicy.ensureLength(tooLong, AiStyle.SHARP, store.getRandom(AiStyle.SHARP));
        assertTrue(AiReplyPolicy.isLengthValid(normalizedLong));
    }

    @Test
    void shouldNormalizeWhitespaceAndLineBreaks() {
        String normalized = AiReplyPolicy.normalize("  先别急着否定自己，\n你只是正在经历一段低潮。  ");
        assertFalse(normalized.contains("\n"));
        assertFalse(normalized.startsWith(" "));
        assertFalse(normalized.endsWith(" "));
    }
}
