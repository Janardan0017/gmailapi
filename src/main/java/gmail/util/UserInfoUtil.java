package gmail.util;

import gmail.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by emp350 on 18/02/21
 */
public class UserInfoUtil {

    public static final Pattern pattern = Pattern.compile("(.+)\\s<(.+)>");

    public static UserInfo getUserInfo(String s) {
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return new UserInfo(matcher.group(1).trim(), matcher.group(2).trim());
        } else {
            return new UserInfo(null, s.trim());
        }
    }

    public static String getEmailId(String s) {
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(2).trim();
        } else {
            return s.trim();
        }
    }

    public static List<String> getEmailIds(String s) {
        List<String> emailIds = new ArrayList<>();
        String[] users = s.split(",");
        for (String user : users) {
            emailIds.add(UserInfoUtil.getEmailId(user));
        }
        return emailIds;
    }
}
