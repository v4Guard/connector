package io.v4guard.connector.common.compatibility;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ComponentAdapter<T> {

    protected final MiniMessage miniMessage;
    protected final LegacyComponentSerializer legacyComponentSerializer;

    protected static final String traceIdentifier = "§8ID#";

    public ComponentAdapter() {
        this.miniMessage = MiniMessage
                .builder()
                .tags(StandardTags.forPreset(MiniMessage.Preset.NON_INTERACTABLE))
                .build();

        this.legacyComponentSerializer = LegacyComponentSerializer
                .builder()
                .character('&')
                .hexCharacter('#')
                .hexColors()
                .build();
    }

    public abstract T adapt(String incomingText);
    public abstract T adapt(List<String> incomingText);

    public abstract T adapt(String incomingText, boolean shouldDownsample);
    public abstract T adapt(List<String> incomingText, boolean shouldDownsample);


    public T adapt(String incomingText, Map<String, String> placeholders) {
        if (placeholders == null) {
            return this.adapt(incomingText);
        }

        String message = incomingText;

        for (String var : placeholders.keySet()) {
            message = message.replace("{" + var + "}", placeholders.get(var));
        }

        return this.adapt(message);
    }

    public T adapt(List<String> incomingText, Map<String, String> placeholders) {
        if (placeholders == null) {
            return this.adapt(incomingText);
        }

        List<String> message = new ArrayList<>();

        for (String line : incomingText) {
            for (String var : placeholders.keySet()) {
                line = line.replace("{" + var + "}", placeholders.get(var));
            }

            message.add(line);
        }

        return this.adapt(message);
    }

    // H&F

    protected static boolean hasLegacyCode(String s) {
        if (s.indexOf('&') < 0 && s.indexOf('§') < 0) return false;

        for (int i = 0, end = s.length() - 1; i < end; i++) {
            char c = s.charAt(i);
            if ((c == '&' || c == '§') && isCodeChar(s.charAt(i + 1))) return true;
        }

        return false;
    }

    protected static boolean isCodeChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')
                || (c >= 'k' && c <= 'o') || (c >= 'K' && c <= 'O')
                || c == 'r' || c == 'R'
                || c == 'x' || c == 'X'
                || c == '#';
    }

}
