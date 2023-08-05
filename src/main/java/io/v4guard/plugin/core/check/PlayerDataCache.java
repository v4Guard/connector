package io.v4guard.plugin.core.check;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import io.v4guard.plugin.core.CoreInstance;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerDataCache {

    private final Cache<String, PlayerCheckData> USERNAME_TO_DATA_CACHE;

    public PlayerDataCache() {
        this.USERNAME_TO_DATA_CACHE = CacheBuilder
                .newBuilder()
                //Checks after that time will fail and return USER_ALLOWED
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .removalListener(listener -> {
                    if (listener.getCause() == RemovalCause.EXPIRED) {
                        CoreInstance.get().getPlugin().getCheckProcessor().forceComplete((PlayerCheckData) listener.getValue());
                    }
                })
                .build();
    }

    public void invalidateAllThatNot(CheckStatus status) {
        for (Map.Entry<String, PlayerCheckData> entry : USERNAME_TO_DATA_CACHE.asMap().entrySet()) {
            if (entry.getValue().getCheckStatus() != status) {
                this.cleanupChecks(entry.getKey());
            }
        }
    }

    public void cache(String username, PlayerCheckData checkData) {
        USERNAME_TO_DATA_CACHE.put(username, checkData);
    }

    public PlayerCheckData get(String username) {
        return USERNAME_TO_DATA_CACHE.getIfPresent(username);
    }

    public void cleanup() {
        USERNAME_TO_DATA_CACHE.cleanUp();
    }

    public void cleanupChecks(String username) {
        USERNAME_TO_DATA_CACHE.invalidate(username);
    }
}
