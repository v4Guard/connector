package io.v4guard.plugin.core.compatibility;

import io.v4guard.plugin.core.accounts.MessageReceiver;
import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.check.brand.BrandCheckProcessor;

import java.io.File;
import java.util.concurrent.TimeUnit;

public interface UniversalPlugin {

    String getPluginName();
    boolean isPluginEnabled(String pluginName);
    File getDataFolder();
    String getPlayerServer(String playerName);
    PlayerFetchResult<?> fetchPlayer(String playerName);
    void kickPlayer(String playerName, String reason);
    Messenger getMessenger();
    MessageReceiver getMessageReceiver();
    CheckProcessor<?> getCheckProcessor();
    BrandCheckProcessor getBrandCheckProcessor();
    int schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit);
    void cancelTask(int taskId);

}