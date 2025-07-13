package io.v4guard.connector.common.check.settings;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.api.constants.SettingsKeys;
import io.v4guard.connector.common.socket.settings.DefaultActiveSettings;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerSettingsCheckProcessor {

    private final Cache<UUID, PlayerSettingsCallbackTask> PENDING_CHECKS = Caffeine
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .removalListener((key, value, cause) -> ((PlayerSettingsCallbackTask) value).complete())
            .build();


    public void process(String username, UUID playerUUID, MinecraftSettings settings) {
        if (!CoreInstance.get().getRemoteConnection().isReady()) {
            return;
        }

        DefaultActiveSettings defaultActiveSettings = CoreInstance.get().getActiveSettings();
        boolean invalidatedCache = defaultActiveSettings.getGeneralSetting(SettingsKeys.INVALIDATE_CACHE, false);
        boolean collectSettings = defaultActiveSettings.getPrivacySetting(SettingsKeys.COLLECT_PLAYER_SETTINGS);

        if (invalidatedCache && !collectSettings) {
            return;
        }

        PlayerCheckData checkData = CoreInstance.get().getCheckDataCache().get(username);

        if (checkData == null || checkData.isPlayerSettingsChecked()) {
            return;
        }

        PlayerSettingsCallbackTask task = PENDING_CHECKS.getIfPresent(playerUUID);

        if (task == null) {
            task = new PlayerSettingsCallbackTask(username, checkData);
            task.start();
            PENDING_CHECKS.put(playerUUID, task);
        }

        task.setPlayerSettings(settings.getMainSettingsMap());
        task.setSkinSettings(settings.getSkinSettingsMap());
    }

    public void onPlayerDisconnect(UUID playerUUID) {
        PENDING_CHECKS.invalidate(playerUUID);
    }

    public void handleTick() {
        PENDING_CHECKS.cleanUp();
    }

}