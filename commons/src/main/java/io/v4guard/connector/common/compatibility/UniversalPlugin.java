package io.v4guard.connector.common.compatibility;

import io.v4guard.connector.common.accounts.MessageReceiver;
import io.v4guard.connector.common.cache.CheckDataCache;
import io.v4guard.connector.common.check.CheckProcessor;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;

import java.io.File;
import java.util.concurrent.TimeUnit;

public interface UniversalPlugin {

    String getPluginName();
    boolean isPluginEnabled(String pluginName);
    File getDataFolder();
    PlayerFetchResult<?> fetchPlayer(String playerName);
    void kickPlayer(String playerName, String reason);
    void kickPlayer(String playerName, String reason, boolean later);
    UniversalTask schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit);
    Messenger getMessenger();
    CheckDataCache getCheckDataCache();
    MessageReceiver getMessageReceiver();
    CheckProcessor<?> getCheckProcessor();
    BrandCheckProcessor getBrandCheckProcessor();
    PlayerSettingsCheckProcessor getPlayerSettingsCheckProcessor();

}
