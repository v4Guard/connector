package io.v4guard.connector.common.socket;

import io.v4guard.connector.common.UnifiedLogger;

import java.util.regex.Pattern;

public class NameValidator  {

    private final boolean isEnabled;
    private final String regex;
    private Pattern pattern = null;

    public NameValidator(boolean isEnabled, String regex) {
        this.isEnabled = isEnabled;
        this.regex = regex;

        if (isEnabled) {
            try {
                pattern =  Pattern.compile(regex);
            } catch (Exception e) {
                UnifiedLogger.get().severe("Invalid regex pattern: " + regex);
            }
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getRegex() {
        return regex;
    }

    public boolean isValid(String name) {
        return !isEnabled || pattern.matcher(name).matches();
    }

}