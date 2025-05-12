package io.v4guard.connector.common.socket;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.v4guard.connector.api.socket.ActiveSettings;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.serializer.ActiveSettingsDeserializer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@JsonDeserialize(using = ActiveSettingsDeserializer.class)
public class DefaultActiveSettings implements ActiveSettings {

    private ConcurrentHashMap<String, Boolean> general;
    private NameValidator nameValidator;
    private ConcurrentHashMap<String, List<String>> messages;
    private ConcurrentHashMap<String, Boolean> activeAddons;
    private ConcurrentHashMap<String, Boolean> privacySettings;


    public DefaultActiveSettings(ConcurrentHashMap<String, Boolean> general,
                                 ConcurrentHashMap<String, List<String>> messages,
                                 NameValidator nameValidator,
                                 ConcurrentHashMap<String, Boolean> activeAddons,
                                 ConcurrentHashMap<String, Boolean> privacySettings
    ) {
        this.general = general;
        this.nameValidator = nameValidator;
        this.messages = messages;
        this.activeAddons = activeAddons;
        this.privacySettings = privacySettings;
    }

    public DefaultActiveSettings() {
        this.general = new ConcurrentHashMap<>();
        this.messages = new ConcurrentHashMap<>();
        this.activeAddons = new ConcurrentHashMap<>();
        this.privacySettings = new ConcurrentHashMap<>();
    }

    @Override
    public ConcurrentHashMap<String, Boolean> getGeneral() {
        return general;
    }

    public ConcurrentHashMap<String, List<String>> getMessages() {
        return messages;
    }

    public NameValidator getNameValidator() {
        return nameValidator;
    }
    @Override
    public ConcurrentHashMap<String, Boolean> getActiveAddons() {
        return activeAddons;
    }
    @Override
    public ConcurrentHashMap<String, Boolean> getPrivacySettings() {
        return privacySettings;
    }

    public void setGeneral(ConcurrentHashMap<String, Boolean> general) {
        this.general = general;
    }

    public void setNameValidator(NameValidator nameValidator) {
        this.nameValidator = nameValidator;
    }

    public void setMessages(ConcurrentHashMap<String, List<String>> messages) {
        this.messages = messages;
    }

    public void setActiveAddons(ConcurrentHashMap<String, Boolean> activeAddons) {
        this.activeAddons = activeAddons;
    }

    public void setPrivacySettings(ConcurrentHashMap<String, Boolean> privacySettings) {
        this.privacySettings = privacySettings;
    }

    public void updateGeneralValue(String key, Boolean value) {
        general.put(key, value);
    }

    public void updateAddonState(String key, Boolean value) {
        activeAddons.put(key, value);
    }

    public void updateMessage(String key, List<String> value) {
        messages.put(key, value);
    }

    public void updatePrivacyState(String key, Boolean value) {
        privacySettings.put(key, value);
    }

    public Boolean getGeneralSetting(String key, Boolean defaultValue) {
        return general.getOrDefault(key, defaultValue);
    }

    public Boolean getAddonState(String key, Boolean defaultValue) {
        return activeAddons.getOrDefault(key, defaultValue);
    }

    public List<String> getMessage(String key) {
        return messages.getOrDefault(key, List.of("Protocol error"));
    }

    public boolean getPrivacySetting(String key) {
        return privacySettings.getOrDefault(key, true );
    }

    public static class NameValidator {

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
}