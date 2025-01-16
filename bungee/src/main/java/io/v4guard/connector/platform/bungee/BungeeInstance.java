package io.v4guard.connector.platform.bungee;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;
import io.v4guard.connector.common.compatibility.PlayerFetchResult;
import io.v4guard.connector.common.compatibility.ServerPlatform;
import io.v4guard.connector.common.compatibility.UniversalPlugin;
import io.v4guard.connector.common.compatibility.UniversalTask;
import io.v4guard.connector.platform.bungee.adapter.BungeeMessenger;
import io.v4guard.connector.platform.bungee.cache.BungeeCheckDataCache;
import io.v4guard.connector.platform.bungee.check.BungeeCheckProcessor;
import io.v4guard.connector.platform.bungee.listener.PlayerListener;
import io.v4guard.connector.platform.bungee.listener.PlayerSettingsListener;
import io.v4guard.connector.platform.bungee.listener.PluginMessagingListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeInstance extends Plugin implements UniversalPlugin {

    private static BungeeInstance instance;
    private BungeeMessenger messenger;
    private BungeeCheckDataCache checkDataCache;
    private BungeeCheckProcessor checkProcessor;
    private PluginMessagingListener brandCheckProcessor;
    private PlayerSettingsListener playerSettingsProcessor;
    private CoreInstance coreInstance;

    private final int METRICS = 16219;

    @Override
    public void onEnable() {
        getLogger().info("(Bungee) Enabling...");
        getLogger().warning("(Bungee) Remember to allow Metrics on your firewall.");

        ProxyServer.getInstance().getScheduler().runAsync(this, () -> new Metrics(this, METRICS));

        instance = this;

        this.checkProcessor = new BungeeCheckProcessor(this);
        this.brandCheckProcessor = new PluginMessagingListener();
        this.playerSettingsProcessor = new PlayerSettingsListener();
        this.messenger = new BungeeMessenger();
        this.checkDataCache = new BungeeCheckDataCache();

        try {
            this.coreInstance = new CoreInstance(ServerPlatform.BUNGEE, this);
            this.coreInstance.initialize();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "(Bungee) Enabling... [ERROR]", exception);
            return;
        }


        //this.getProxy().registerChannel(MessageReceiver.CHANNEL);
        this.getProxy().getPluginManager().registerListener(this, this.brandCheckProcessor);
        this.getProxy().getPluginManager().registerListener(this, this.playerSettingsProcessor);
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this, coreInstance));

        getLogger().info("(Bungee) Enabling... [DONE]");
    }

    @Override
    public void onDisable() {
        getLogger().info("(Bungee) Disabling...");
        getLogger().info("(Bungee) Disconnecting from the backend...");

        try {
            this.coreInstance.getRemoteConnection().disconnect();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "(Bungee) Disabling... [ERROR]", exception);
            return;
        }

        getLogger().info("(Bungee) Disabling... [DONE]");
    }

    public static BungeeInstance get() {
        return instance;
    }

    public String getPluginName() {
        return getDescription().getName();
    }

    public boolean isPluginEnabled(String pluginName) {
        return this.getProxy().getPluginManager().getPlugin(pluginName) != null;
    }

    @Override
    public PlayerFetchResult<ProxiedPlayer> fetchPlayer(String playerName) {
        ProxiedPlayer player = getProxy().getPlayer(playerName);

        if (player == null) {
            return new PlayerFetchResult<>(null, null, false);
        }

        Server server = player.getServer();

        return new PlayerFetchResult<>(
                player
                , server == null ? null : server.getInfo().getName()
                , true
        );
    }

    @Override
    public void kickPlayer(String playerName, String reason) {
        kickPlayer(playerName, reason, false);
    }

    public void kickPlayer(String playerName, String reason, boolean later) {
        PlayerFetchResult<ProxiedPlayer> fetchedPlayer = fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            fetchedPlayer.getPlayer().disconnect(TextComponent.fromLegacy(reason));
        }
    }

    @Override
    public UniversalTask schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return new BungeeTask(getProxy().getScheduler().schedule(this, runnable, delay, period, timeUnit));
    }

    @Override
    public BungeeMessenger getMessenger() {
        return messenger;
    }

    @Override
    public BungeeCheckDataCache getCheckDataCache() {
        return checkDataCache;
    }

    @Override
    public BungeeCheckProcessor getCheckProcessor() {
        return checkProcessor;
    }

    @Override
    public BrandCheckProcessor getBrandCheckProcessor() {
        return brandCheckProcessor;
    }

    @Override
    public PlayerSettingsCheckProcessor getPlayerSettingsCheckProcessor() {
        return playerSettingsProcessor;
    }
}
