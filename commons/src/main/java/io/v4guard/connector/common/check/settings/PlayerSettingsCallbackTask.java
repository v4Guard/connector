package io.v4guard.connector.common.check.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.CallbackTask;
import io.v4guard.connector.common.check.PlayerCheckData;

import java.util.HashMap;
import java.util.Map;

public class PlayerSettingsCallbackTask extends CallbackTask {


    private Map<String, String> settings;
    private Map<String, String> skinParts;

    public PlayerSettingsCallbackTask(String taskId, PlayerCheckData playerCheckData) {
        super(taskId, playerCheckData);
        settings = new HashMap<>();
        skinParts = new HashMap<>();
    }

    @Override
    public void complete() {
        super.checkData.setPlayerSettingsChecked(true);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode settingsDocument = CoreInstance.get().getObjectMapper().createObjectNode();

        settingsDocument.put("username", super.checkData.getUsername());
        settingsDocument.put("address", super.checkData.getAddress());
        settingsDocument.putArray("settings").add(mapper.valueToTree(settings));
        settingsDocument.putArray("skin").add(mapper.valueToTree(skinParts));

        CoreInstance.get().getRemoteConnection().send("mc:settings", settingsDocument);
    }

    public void setPlayerSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public void setSkinSettings(Map<String, String> skinParts) {
        this.skinParts = skinParts;
    }
}
