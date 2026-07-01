package com.mindecho.util;

import com.mindecho.model.EmotionLabel;

import java.util.List;
import java.util.Locale;

public class EmotionClassifier {
    private static final List<String> ANGER_KEYWORDS = List.of(
            "愤怒", "生气", "气死", "气炸", "气疯", "火大", "暴躁", "恼火", "烦死", "烦透",
            "烦死了", "受不了", "忍不了", "恶心", "讨厌", "恨", "想骂", "想砸", "崩溃",
            "暴怒", "发脾气", "发火", "大怒", "怒气", "怒不可遏", "怒火中烧", "气愤",
            "愤慨", "恼羞成怒", "咬牙切齿", "暴跳如雷", "怒发冲冠", "拍桌子", "摔东西",
            "气死我了", "真让人火大", "太过分了", "简直不可理喻", "莫名其妙", "无法忍受",
            "太离谱", "气死我了", "忍无可忍", "火冒三丈", "怒气冲冲", "愤愤不平",
            "不爽", "不满", "憋屈", "窝火", "恼火", "气不打一处来", "越想越气",
            "讨厌死了", "烦死人了", "真烦人", "让人抓狂", "想骂人", "想打人",
            "气死我了", "我要炸了", "怒火攻心", "怒气腾腾", "大发雷霆", "怒目圆睁",
            "吹胡子瞪眼", "脸红脖子粗", "青筋暴起", "咬牙切齿", "握紧拳头", "跺着脚"
    );
    private static final List<String> ANXIETY_KEYWORDS = List.of(
            "焦虑", "担心", "不安", "睡不着", "失眠", "压力", "紧张", "害怕", "恐惧", "担忧",
            "慌", "慌张", "心慌", "忐忑", "没底", "怎么办", "怕来不及", "怕出错",
            "不安", "焦躁", "忧虑", "发愁", "愁眉苦脸", "忧心忡忡", "心神不宁",
            "坐立不安", "寝食难安", "如坐针毡", "热锅上的蚂蚁", "七上八下",
            "忐忑不安", "心神不定", "心慌意乱", "心烦意乱", "惴惴不安", "惶惶不安",
            "不安分", "放不下心", "惦记", "挂心", "放不下", "总是想着", "老是担心",
            "生怕", "唯恐", "万一", "怕", "担心", "害怕", "恐惧", "畏惧", "害怕失败",
            "害怕出错", "害怕被拒绝", "害怕被批评", "害怕被嘲笑", "害怕失去",
            "压力大", "压力好大", "有压力", "感到压力", "承受压力", "压力山大",
            "紧张", "很紧张", "特别紧张", "紧张不安", "紧张兮兮", "神经紧张",
            "焦虑不安", "焦虑症", "焦虑情绪", "越来越焦虑", "非常焦虑", "极度焦虑",
            "失眠", "睡不着觉", "睡不好", "难以入睡", "半夜醒来", "凌晨醒来",
            "辗转反侧", "难以入眠", "无法入睡", "睡眠不好", "睡眠障碍",
            "怎么办", "怎么办啊", "这可怎么办", "不知道怎么办", "毫无头绪",
            "不知所措", "无所适从", "手足无措", "无从下手", "毫无办法", "束手无策",
            "迷茫", "困惑", "不知所措", "不知道该怎么办", "没有方向", "迷茫无助",
            "不安", "内心不安", "心里不安", "感到不安", "莫名不安", "隐隐不安"
    );
    private static final List<String> SADNESS_KEYWORDS = List.of(
            "难过", "委屈", "想哭", "哭了", "伤心", "失落", "悲伤", "痛苦", "难受", "低落",
            "孤独", "心酸", "无助", "压抑", "沮丧", "绝望", "好累", "撑不住",
            "难过", "伤心", "悲伤", "哀伤", "悲痛", "伤感", "哀痛", "痛心",
            "心如刀割", "伤心欲绝", "悲痛欲绝", "泪流满面", "痛哭流涕", "泣不成声",
            "黯然神伤", "愁眉不展", "愁眉苦脸", "闷闷不乐", "郁郁寡欢", "闷闷不乐",
            "心情沉重", "心灰意冷", "心如死灰", "万念俱灰", "灰心丧气", "垂头丧气",
            "无精打采", "萎靡不振", "颓丧", "消沉", "情绪低落", "心情低落",
            "闷闷不乐", "郁郁寡欢", "黯然失色", "黯然神伤", "伤心难过",
            "孤独", "孤单", "寂寞", "独自一人", "形单影只", "孤孤单单",
            "没有人理解", "没有人关心", "没有人在乎", "感到孤独", "孤独无助",
            "委屈", "受委屈", "感到委屈", "心里委屈", "很委屈", "太委屈",
            "累", "好累", "非常累", "身心俱疲", "筋疲力尽", "精疲力竭",
            "疲惫不堪", "累垮了", "累坏了", "累到不行", "心力交瘁",
            "无助", "感到无助", "无能为力", "无可奈何", "没办法", "束手无策",
            "绝望", "感到绝望", "陷入绝望", "绝望无助", "走投无路", "山穷水尽",
            "压抑", "感到压抑", "压抑不住", "无法释放", "憋得慌", "堵得慌",
            "想哭", "好想哭", "忍不住哭", "眼泪流下来", "眼眶红了", "泪流满面",
            "痛苦", "好痛苦", "非常痛苦", "痛苦不堪", "苦不堪言", "受尽折磨",
            "失落", "感到失落", "很失落", "非常失落", "无比失落", "怅然若失",
            "沮丧", "感到沮丧", "很沮丧", "非常沮丧", "灰心沮丧", "沮丧失望",
            "心碎", "心都碎了", "心碎一地", "伤心欲绝", "肝肠寸断", "撕心裂肺",
            "难过到极点", "伤心透了", "悲痛万分", "痛苦万分", "难受极了"
    );

    public static EmotionLabel classify(String text) {
        String normalized = normalize(text);
        if (normalized.isBlank()) {
            return EmotionLabel.CALM;
        }

        int angerScore = score(normalized, ANGER_KEYWORDS);
        int anxietyScore = score(normalized, ANXIETY_KEYWORDS);
        int sadnessScore = score(normalized, SADNESS_KEYWORDS);

        if (angerScore == 0 && anxietyScore == 0 && sadnessScore == 0) {
            return EmotionLabel.CALM;
        }

        if (angerScore >= anxietyScore && angerScore >= sadnessScore) {
            return EmotionLabel.ANGER;
        }
        if (anxietyScore >= sadnessScore) {
            return EmotionLabel.ANXIETY;
        }
        return EmotionLabel.SADNESS;
    }

    private static int score(String text, List<String> keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                score += keyword.length() >= 3 ? 2 : 1;
            }
        }

        long punctuationCount = text.chars()
                .filter(ch -> ch == '!' || ch == '！' || ch == '?' || ch == '？')
                .count();
        if (score > 0 && punctuationCount >= 2) {
            score++;
        }
        return score;
    }

    private static String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }
}
