package io.v4guard.connector.platform.bungee.cache;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.v4guard.connector.common.cache.CheckDataCache;
import io.v4guard.connector.common.check.CallbackTask;
import io.v4guard.connector.common.check.PendingTasks;
import io.v4guard.connector.common.check.PlayerCheckData;
import net.md_5.bungee.api.ProxyServer;

import java.util.concurrent.TimeUnit;

/*
 * A helping class that needs to prevent
 * incorrect cache state and memory leaks.
 */
public class BungeeCheckDataCache extends CheckDataCache {

    private final Cache<String, PlayerCheckData> TEMPORAL_CACHE = Caffeine
            .newBuilder()
            .expireAfterWrite(
                    ProxyServer.getInstance().getConfig().getTimeout()
                    , TimeUnit.MILLISECONDS
            ).build();


    public void rememberLogin(String username, PlayerCheckData checkData) {
        TEMPORAL_CACHE.put(username, checkData);
    }

    public PlayerCheckData getTempCheckData(String username) {
        return TEMPORAL_CACHE.getIfPresent(username);
    }

    @Override
    public void handleTick(PendingTasks pendingTasks) {
        TEMPORAL_CACHE.cleanUp();

        for (PlayerCheckData checkData : TEMPORAL_CACHE.asMap().values()) {
            checkData.checkAndTriggerIfExpired();
        }

        super.handleTick(pendingTasks);
    }
}
