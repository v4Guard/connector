package io.v4guard.plugin.core.socket;

import io.v4guard.plugin.core.constants.SettingsKeys;
import io.v4guard.plugin.core.utils.StringUtils;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteSettings {

    private static final ConcurrentHashMap<String, Object> SETTINGS = new ConcurrentHashMap<>();

    public static void updateValue(String key, Object value) {
        SETTINGS.put(key, value);
    }

    public static <T> T getOrDefault(String key, T def) {
        return (T) SETTINGS.getOrDefault(key, def);
    }

    public static <T> T get(String key) {
        return (T) SETTINGS.get(key);
    }

    public static String getMessage(String key) {
        Document messages = get(SettingsKeys.MESSAGES);

        return StringUtils.buildMultilineString((List<String>) messages.get(key));
    }

    public static boolean hasData() {
        return !SETTINGS.isEmpty();
    }

    public static void overrideBy(Map<String, Object> newSettings) {
        SETTINGS.clear();
        SETTINGS.putAll(newSettings);
    }
}
