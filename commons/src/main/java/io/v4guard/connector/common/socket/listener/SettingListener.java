package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.socket.DefaultActiveSettings;

import java.util.ArrayList;

public class SettingListener implements Emitter.Listener {

    private enum Type {
        GENERAL,
        MESSAGE,
        PRIVACY,
        NAME_VALIDATOR,
        ADDON
    }

    @Override
    public void call(Object... args) {
        JsonNode json = CoreInstance.get().readTree(args[0].toString());
        DefaultActiveSettings settings = CoreInstance.get().getActiveSettings();

        Type type = Type.valueOf(json.get("type").asText());

        switch (type) {
            case GENERAL:
                settings.updateGeneralValue(json.get("key").asText(), json.get("value").asBoolean());
                break;
            case MESSAGE:
                settings.updateMessage(json.get("key").asText(), CoreInstance.get().getObjectMapper().convertValue(
                        json.get("message"),
                        new TypeReference<ArrayList<String>>() {})
                );
                break;
            case PRIVACY:
                settings.updatePrivacyState(json.get("key").asText(), json.get("value").asBoolean());
                break;
            case NAME_VALIDATOR:
                settings.setNameValidator(
                        CoreInstance.get().getObjectMapper().convertValue(json.get("value"), DefaultActiveSettings.NameValidator.class)
                );
                break;
            case ADDON:
                settings.updateAddonState(json.get("key").asText(), json.get("value").asBoolean());
                break;
        }
    }

}
