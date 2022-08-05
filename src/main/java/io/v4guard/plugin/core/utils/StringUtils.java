package io.v4guard.plugin.core.utils;

import org.bson.Document;

import java.util.HashMap;
import java.util.List;

public class StringUtils {

    public static String replacePlaceholders(String message, Document placeholders){
        for(String var : placeholders.keySet()){
            message = message.replace("{" + var + "}", placeholders.get(var).toString());
        }
        return message;
    }

    public static String buildMultilineString(List<String> message){
        String messageString = "";
        for (String s : message) {
            messageString += s + "\n";
        }
        return messageString;
    }

}
