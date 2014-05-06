package info.justaway.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.JustawayApplication;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class TwitterUtil {

    private static final Pattern URL_PATTERN = Pattern.compile("(http://|https://)[\\w\\.\\-/:#\\?=&;%~\\+]+");

    /**
     * ツイートの文字数を数えます
     * @param str ツイート文字列
     * @return 文字数
     */
    public static int count(String str) {
        int length = str.codePointCount(0, str.length());

        // 短縮URLを考慮
        Matcher matcher = URL_PATTERN.matcher(str);
        while (matcher.find()) {
            length = length - matcher.group().length() + 22;
            if (matcher.group().contains("https://")) ++length;
        }

        return 140 - length;
    }

    public static String getClientName(String source) {
        String[] tokens = source.split("[<>]");
        if (tokens.length > 1) {
            return tokens[2];
        } else {
            return tokens[0];
        }
    }

    public static boolean isMentionForMe(Status status) {
        long userId = JustawayApplication.getApplication().getUserId();
        if (status.getInReplyToUserId() == userId) {
            return true;
        }
        UserMentionEntity[] mentions = status.getUserMentionEntities();
        for (UserMentionEntity mention : mentions) {
            if (mention.getId() == userId) {
                return true;
            }
        }
        return false;
    }
}
