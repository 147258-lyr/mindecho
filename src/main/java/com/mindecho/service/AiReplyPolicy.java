package com.mindecho.service;

import com.mindecho.model.AiStyle;

public final class AiReplyPolicy {
    public static final int MIN_LENGTH = 15;
    public static final int MAX_LENGTH = 40;

    private AiReplyPolicy() {
    }

    public static String buildSystemPrompt(AiStyle style) {
        if (style == AiStyle.GENTLE) {
            return "你是一位温和包容的中文倾听者。请用委婉、安抚、接纳的语气回应，"
                    + "只输出15到40个中文字符，不分点、不换行，不要解释规则。";
        }
        return "你是一位清醒直接的中文倾听者。请用明确、凝练、有边界感的语气回应，"
                + "只输出15到40个中文字符，不分点、不换行，不做人身攻击，不要解释规则。";
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\r\\n]+", "")
                .replaceAll("\\s+", "")
                .trim();
    }

    public static boolean isLengthValid(String text) {
        int length = normalize(text).length();
        return length >= MIN_LENGTH && length <= MAX_LENGTH;
    }

    public static String ensureLength(String text, AiStyle style, String fallback) {
        String normalized = normalize(text);
        if (isLengthValid(normalized)) {
            return normalized;
        }
        return normalize(fallbackFor(style, fallback));
    }

    private static String fallbackFor(AiStyle style, String fallback) {
        if (isLengthValid(fallback)) {
            return fallback;
        }
        return style == AiStyle.GENTLE
                ? "先别急着责怪自己，你已经在认真撑住这一切。"
                : "情绪可以翻涌，但别让它替你决定下一步。";
    }
}
