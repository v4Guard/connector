package io.v4guard.connector.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.CallbackTask;
import io.v4guard.connector.common.check.CheckStatus;
import io.v4guard.connector.common.check.PendingTasks;
import io.v4guard.connector.common.check.PlayerCheckData;

import java.util.Map;

public class CheckDataCache {

    private final Cache<String, PlayerCheckData> USERNAME_TO_DATA_CACHE;

    public CheckDataCache() {
        this.USERNAME_TO_DATA_CACHE = Caffeine.newBuilder().build();
    }

    public void invalidateAllThatNot(CheckStatus status) {
        for (Map.Entry<String, PlayerCheckData> entry : USERNAME_TO_DATA_CACHE.asMap().entrySet()) {
            if (entry.getValue().getCheckStatus() != status) {
                this.cleanup(entry.getKey());
            }
        }
    }

    public void handleTick(PendingTasks pendingTasks) {
        for (PlayerCheckData checkData : USERNAME_TO_DATA_CACHE.asMap().values()) {
            CallbackTask task = checkData.getCurrentTask();

            if (checkData.checkAndTriggerIfExpired()) {
                pendingTasks.remove(task.getTaskID());
            }
        }
    }

    public void cache(String username, PlayerCheckData checkData) {
        USERNAME_TO_DATA_CACHE.put(username, checkData);
    }

    public PlayerCheckData get(String username) {
        return USERNAME_TO_DATA_CACHE.getIfPresent(username);
    }

    public void cleanup(String username) {
        USERNAME_TO_DATA_CACHE.invalidate(username);
    }
}
