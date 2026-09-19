package io.v4guard.connector.common.utils;


import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;

//
public class BannerMessage {

    private static final String INDENT = "        ";
    private static final String[] LOGO = {
            "              ████████████",
            "                          ███",
            "            ●  ██████████   ██",
            "                             ██",
            "             ███████████ ●   ██",
            "                             ██",
            "            ●  ██████████   ██",
            "                          ███",
            "              ████████████",
    };

    private final List<String> body = new ArrayList<>();

    private BannerMessage() {}

    public static BannerMessage create() {
        return new BannerMessage();
    }

    public BannerMessage line(String text, Object... args) {
        String content = (args == null || args.length == 0) ? text : String.format(text, args);
        body.add(content.isEmpty() ? "" : INDENT + content);
        return this;
    }


    public BannerMessage lines(List<String> lines) {
        body.addAll(lines.stream().map(line -> line.isEmpty() ? "" : INDENT + line).toList());
        return this;
    }

    public BannerMessage blank() {
        body.add("");
        return this;
    }

    public String build() {
        StringJoiner joiner = new StringJoiner("\n");

        joiner.add("").add("").add("").add("");
        for (String logoLine : LOGO) {
            joiner.add(logoLine);
        }

        joiner.add("").add("");
        joiner.add(INDENT + String.format("v4Guard Connector (v%s)", CoreInstance.PLUGIN_VERSION));

        if (!body.isEmpty()) {
            joiner.add("");
            for (String bodyLine : body) {
                joiner.add(bodyLine);
            }
        }

        joiner.add("").add("").add("");
        return joiner.toString();
    }

    public void info() {
        UnifiedLogger.get().info(build());
    }

    public void warning() {
        UnifiedLogger.get().warning(build());
    }

    public void error() {
        UnifiedLogger.get().log(Level.SEVERE, build());
    }

    public void log(Level level) {
        UnifiedLogger.get().log(level, build());
    }

}
