package io.v4guard.connector.common.socket.settings;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.v4guard.connector.api.socket.ActiveSettings;
import io.v4guard.connector.api.socket.Addon;
import io.v4guard.connector.common.serializer.ActiveSettingsDeserializer;
import io.v4guard.connector.common.socket.NameValidator;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@JsonDeserialize(using = ActiveSettingsDeserializer.class)
public class DefaultActiveSettings implements ActiveSettings {

    private ConcurrentHashMap<String, Boolean> general;
    private NameValidator nameValidator;
    private ConcurrentHashMap<String, List<String>> messages;
    private ConcurrentHashMap<String, Addon> activeAddons;
    private ConcurrentHashMap<String, Boolean> privacySettings;


    public DefaultActiveSettings(ConcurrentHashMap<String, Boolean> general,
                                 ConcurrentHashMap<String, List<String>> messages,
                                 NameValidator nameValidator,
                                 ConcurrentHashMap<String, Addon> activeAddons,
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
    public ConcurrentHashMap<String, Addon> getActiveAddons() {
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

    public void setActiveAddons(ConcurrentHashMap<String, Addon> activeAddons) {
        this.activeAddons = activeAddons;
    }

    public void setPrivacySettings(ConcurrentHashMap<String, Boolean> privacySettings) {
        this.privacySettings = privacySettings;
    }

    public void updateGeneralValue(String key, Boolean value) {
        general.put(key, value);
    }

    public void updateAddonState(String key, Addon value) {
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

    public Addon getAddonState(String key, Addon defaultValue) {
        return activeAddons.getOrDefault(key, defaultValue);
    }

    public List<String> getMessage(String key) {
        return messages.getOrDefault(key, List.of("Protocol error"));
    }

    public boolean getPrivacySetting(String key) {
        return privacySettings.getOrDefault(key, true );
    }
}