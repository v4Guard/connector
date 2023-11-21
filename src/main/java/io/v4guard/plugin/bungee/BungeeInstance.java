package io.v4guard.plugin.bungee;

import io.v4guard.plugin.bungee.accounts.BungeeMessageReceiver;
import io.v4guard.plugin.bungee.adapter.BungeeMessenger;
import io.v4guard.plugin.bungee.cache.BungeeCheckDataCache;
import io.v4guard.plugin.bungee.check.BungeeCheckProcessor;
import io.v4guard.plugin.bungee.listener.PlayerListener;
import io.v4guard.plugin.bungee.listener.PluginMessagingListener;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.accounts.MessageReceiver;
import io.v4guard.plugin.core.cache.CheckDataCache;
import io.v4guard.plugin.core.check.brand.BrandCheckProcessor;
import io.v4guard.plugin.core.compatibility.PlayerFetchResult;
import io.v4guard.plugin.core.compatibility.ServerPlatform;
import io.v4guard.plugin.core.compatibility.UniversalPlugin;
import io.v4guard.plugin.core.compatibility.UniversalTask;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeInstance extends Plugin implements UniversalPlugin {

    private static BungeeInstance instance;
    private BungeeMessenger messenger;
    private BungeeCheckDataCache checkDataCache;
    private BungeeMessageReceiver messageReceiver;
    private BungeeCheckProcessor checkProcessor;
    private PluginMessagingListener brandCheckProcessor;
    private CoreInstance coreInstance;
    private boolean floodGateFound;

    @Override
    public void onEnable() {
        getLogger().info("(Bungee) Enabling...");
        getLogger().warning("(Bungee) Remember to allow Metrics on your firewall.");

        new Metrics(this, 16219);

        instance = this;

        this.checkProcessor = new BungeeCheckProcessor(this);
        this.brandCheckProcessor = new PluginMessagingListener();
        this.messenger = new BungeeMessenger();
        this.checkDataCache = new BungeeCheckDataCache();
        this.messageReceiver = new BungeeMessageReceiver();

        try {
            this.coreInstance = new CoreInstance(ServerPlatform.BUNGEE, this);
            this.coreInstance.initialize();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "(Bungee) Enabling... [ERROR]", exception);
            return;
        }

        if (isPluginEnabled("floodgate")) {
            floodGateFound = true;
        }

        //this.getProxy().registerChannel(MessageReceiver.CHANNEL);
        this.getProxy().getPluginManager().registerListener(this, this.messageReceiver);
        this.getProxy().getPluginManager().registerListener(this, this.brandCheckProcessor);
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this));

        getLogger().info("(Bungee) Enabling... [DONE]");
    }

    @Override
    public void onDisable() {
        getLogger().info("(Bungee) Disabling...");
        getLogger().info("(Bungee) Disconnecting from the backend...");

        try {
            this.coreInstance.getBackend().getSocket().disconnect();
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

    public void kickPlayer(String playerName, String reason) {
        PlayerFetchResult<ProxiedPlayer> fetchedPlayer = fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            fetchedPlayer.getPlayer().disconnect(TextComponent.fromLegacyText(reason));
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
    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
    }

    @Override
    public BungeeCheckProcessor getCheckProcessor() {
        return checkProcessor;
    }

    @Override
    public BrandCheckProcessor getBrandCheckProcessor() {
        return brandCheckProcessor;
    }

    public boolean isFloodGateFound() {
        return floodGateFound;
    }
}
