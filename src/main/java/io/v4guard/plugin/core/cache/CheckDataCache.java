package io.v4guard.plugin.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.v4guard.plugin.core.check.CheckStatus;
import io.v4guard.plugin.core.check.PlayerCheckData;

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

    public void handleTick() {
        for (PlayerCheckData checkData : USERNAME_TO_DATA_CACHE.asMap().values()) {
            checkData.triggerCompletedIfExpired();
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
