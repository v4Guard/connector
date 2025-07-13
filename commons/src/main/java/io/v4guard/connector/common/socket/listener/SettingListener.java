package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.socket.settings.DefaultActiveSettings;
import io.v4guard.connector.common.socket.NameValidator;
import io.v4guard.connector.common.socket.settings.DefaultAddonSetting;

import java.util.ArrayList;

public class SettingListener implements Emitter.Listener {

    private final CoreInstance coreInstance;
    
    public SettingListener(CoreInstance coreInstance) {
        this.coreInstance = coreInstance;
    }
    
    private enum Type {
        GENERAL,
        MESSAGE,
        PRIVACY,
        NAME_VALIDATOR,
        ADDON
    }

    @Override
    public void call(Object... args) {
        JsonNode json = coreInstance.readTree(args[0].toString());
        DefaultActiveSettings settings = coreInstance.getActiveSettings();

        Type type = Type.valueOf(json.get("type").asText());

        switch (type) {
            case GENERAL:
                settings.updateGeneralValue(json.get("key").asText(), json.get("value").asBoolean());
                break;
            case MESSAGE:
                settings.updateMessage(json.get("key").asText(), coreInstance.getObjectMapper().convertValue(
                        json.get("message"),
                        new TypeReference<ArrayList<String>>() {})
                );
                break;
            case PRIVACY:
                settings.updatePrivacyState(json.get("key").asText(), json.get("value").asBoolean());
                break;
            case NAME_VALIDATOR:
                settings.setNameValidator(
                        coreInstance.getObjectMapper().convertValue(json.get("value"), NameValidator.class)
                );
                break;
            case ADDON:
                settings.updateAddonState(json.get("key").asText(), coreInstance.getObjectMapper().convertValue("value", DefaultAddonSetting.class));
                break;
        }
    }

}
