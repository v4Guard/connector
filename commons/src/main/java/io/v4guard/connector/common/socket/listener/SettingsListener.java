package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.socket.ActiveSettings;

public class SettingsListener implements Emitter.Listener {

    private final CoreInstance coreInstance;

    public SettingsListener(CoreInstance coreInstance) {
        this.coreInstance = coreInstance;
    }

    @Override
    public void call(Object... args) {

        try {
            ActiveSettings activeSettings = coreInstance.getObjectMapper().readValue(args[0].toString(), ActiveSettings.class);
            coreInstance.setActiveSettings(activeSettings);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}