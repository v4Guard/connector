package io.v4guard.connector.common.utils;

import java.util.Map;

public class StringUtils {

    public static String replacePlaceholders(String message, Map<String, String> placeholders){
        if (placeholders == null) {
            return message;
        }

        for (String var : placeholders.keySet()) {
            message = message.replace("{" + var + "}", placeholders.get(var));
        }

        return message;
    }
}
