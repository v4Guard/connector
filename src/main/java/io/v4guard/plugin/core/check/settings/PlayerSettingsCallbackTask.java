package io.v4guard.plugin.core.check.settings;

import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.CallbackTask;
import io.v4guard.plugin.core.check.PlayerCheckData;
import org.bson.Document;

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

        Document settingsDocument = new Document();

        settingsDocument.append("username", super.checkData.getUsername());
        settingsDocument.append("address", super.checkData.getAddress());
        settingsDocument.append("settings", settings);
        settingsDocument.append("skin", skinParts);

        CoreInstance.get().getBackend().getSocket().emit(
                "mc:settings"
                , settingsDocument.toJson()
        );
    }

    public void setPlayerSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public void setSkinSettings(Map<String, String> skinParts) {
        this.skinParts = skinParts;
    }
}
