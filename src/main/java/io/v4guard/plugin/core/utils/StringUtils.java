package io.v4guard.plugin.core.utils;

import org.bson.Document;

import java.util.List;
import java.util.StringJoiner;

public class StringUtils {

    public static String replacePlaceholders(String message, Document placeholders){
        if(placeholders == null) return message;
        for(String var : placeholders.keySet()){
            message = message.replace("{" + var + "}", placeholders.get(var).toString());
        }
        return message;
    }

    public static String buildMultilineString(List<String> lines) {
        StringJoiner message = new StringJoiner("\n");

        for (String line : lines) {
            message.add(line);
        }

        return message.toString();
    }

}
