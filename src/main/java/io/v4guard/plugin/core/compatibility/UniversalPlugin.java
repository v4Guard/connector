package io.v4guard.plugin.core.compatibility;

import io.v4guard.plugin.core.accounts.MessageReceiver;
import io.v4guard.plugin.core.cache.CheckDataCache;
import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.check.brand.BrandCheckProcessor;
import io.v4guard.plugin.core.check.settings.PlayerSettingsCheckProcessor;

import java.io.File;
import java.util.concurrent.TimeUnit;

public interface UniversalPlugin {

    String getPluginName();
    boolean isPluginEnabled(String pluginName);
    File getDataFolder();
    PlayerFetchResult<?> fetchPlayer(String playerName);
    void kickPlayer(String playerName, String reason);
    UniversalTask schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit);
    Messenger getMessenger();
    CheckDataCache getCheckDataCache();
    MessageReceiver getMessageReceiver();
    CheckProcessor<?> getCheckProcessor();
    BrandCheckProcessor getBrandCheckProcessor();
    PlayerSettingsCheckProcessor getPlayerSettingsCheckProcessor();

}
