package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.v4guard.connector.api.socket.Addon;
import io.v4guard.connector.common.serializer.AddonDeserializer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@JsonDeserialize(using = AddonDeserializer.class)
public class DefaultAddon implements Addon {
    private boolean enabled;

    private ConcurrentHashMap<String, String> settings = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<String>> messages = new ConcurrentHashMap<>();

    public DefaultAddon(boolean isEnabled, ConcurrentHashMap<String, String> settings, ConcurrentHashMap<String, List<String>> messages) {
        this.enabled = isEnabled;
        this.settings = settings;
        this.messages = messages;
    }

    public DefaultAddon() { }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public ConcurrentHashMap<String, String> getSettings() {
        return settings;
    }

    @Override
    public ConcurrentHashMap<String, List<String>> getMessages() {
        return messages;
    }

    public void setMessages(ConcurrentHashMap<String, List<String>> messages) {
        this.messages = messages;
    }

    public void setSettings(ConcurrentHashMap<String, String> settings) {
        this.settings = settings;
    }
}
