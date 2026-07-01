package com.mindecho.util;

import com.mindecho.model.EmotionLabel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmotionClassifierTest {

    @Test
    void shouldClassifyAngerFromCommonPhrase() {
        assertEquals(EmotionLabel.ANGER, EmotionClassifier.classify("我真的烦死了，气炸了！！"));
    }

    @Test
    void shouldClassifyAnxietyFromNaturalSentence() {
        assertEquals(EmotionLabel.ANXIETY, EmotionClassifier.classify("最近压力很大，晚上总是睡不着，心里很慌"));
    }

    @Test
    void shouldClassifySadnessFromLowMoodExpression() {
        assertEquals(EmotionLabel.SADNESS, EmotionClassifier.classify("今天特别委屈，整个人都很失落，真的想哭"));
    }

    @Test
    void shouldFallbackToCalmWhenNoSignalsPresent() {
        assertEquals(EmotionLabel.CALM, EmotionClassifier.classify("今天天气不错，我准备去散步"));
    }
}
