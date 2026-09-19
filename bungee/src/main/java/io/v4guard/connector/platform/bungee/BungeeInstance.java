package io.v4guard.connector.platform.bungee;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.v4guard.connector.api.v4GuardConnectorProvider;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;
import io.v4guard.connector.common.compatibility.*;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import io.v4guard.connector.platform.bungee.adapter.BungeeComponentAdapter;
import io.v4guard.connector.platform.bungee.adapter.BungeeMessenger;
import io.v4guard.connector.platform.bungee.cache.BungeeCheckDataCache;
import io.v4guard.connector.platform.bungee.check.BungeeCheckProcessor;
import io.v4guard.connector.platform.bungee.command.ConnectorCommand;
import io.v4guard.connector.platform.bungee.command.sub.BlacklistCommand;
import io.v4guard.connector.platform.bungee.command.sub.WhitelistCommand;
import io.v4guard.connector.platform.bungee.listener.PlayerListener;
import io.v4guard.connector.platform.bungee.listener.PlayerSettingsListener;
import io.v4guard.connector.platform.bungee.listener.PluginMessagingListener;
import io.v4guard.connector.platform.bungee.task.AwaitingKickTask;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.bungee.BungeeCommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeInstance extends Plugin implements UniversalPlugin {

    private static BungeeInstance instance;
    private BungeeMessenger messenger;
    private BungeeCheckDataCache checkDataCache;
    private BungeeCheckProcessor checkProcessor;
    private PluginMessagingListener brandCheckProcessor;
    private PlayerSettingsListener playerSettingsProcessor;
    private Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private ComponentAdapter<BaseComponent[]> componentAdapter;

    private CoreInstance coreInstance;

    private final int METRICS = 16219;

    @Override
    public void onEnable() {
        getLogger().info("(Bungee) Enabling...");
        getLogger().warning("(Bungee) Remember to allow Metrics on your firewall.");

        ProxyServer.getInstance().getScheduler().runAsync(this, () -> new Metrics(this, METRICS));

        instance = this;

        this.componentAdapter = new BungeeComponentAdapter();
        this.checkProcessor = new BungeeCheckProcessor(this);
        this.brandCheckProcessor = new PluginMessagingListener();
        this.playerSettingsProcessor = new PlayerSettingsListener();
        this.messenger = new BungeeMessenger(this.componentAdapter);
        this.checkDataCache = new BungeeCheckDataCache();

        try {
            this.coreInstance = new CoreInstance(ServerPlatform.BUNGEE, this);
            this.coreInstance.initialize();
        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "(Bungee) Enabling... [ERROR]", exception);
            return;
        }

        String propertyConnTimeout = System.getProperty("io.v4guard.connector.connectionTimeout");
        int connectionTimeout = ProxyServer.getInstance().getConfig().getTimeout();
        if (propertyConnTimeout != null && !propertyConnTimeout.trim().isEmpty()) {
            try {
                connectionTimeout = Integer.parseInt(propertyConnTimeout.trim());
            } catch (NumberFormatException e) {
                this.getLogger().warning("Ignoring invalid io.v4guard.connector.connectionTimeout: " + propertyConnTimeout);
            }
        }

        if (connectionTimeout < 3000) {
            getLogger().warning("(Bungee) Connect timeout is lower than 3000ms, forcing our own timeout of 5000ms. You should raise the timeout in your bungee config.");
            connectionTimeout = 5000;
        }

        this.awaitedKickTaskCache = Caffeine
                .newBuilder()
                .expireAfterWrite(connectionTimeout, TimeUnit.MILLISECONDS) //prevent memory leaks in case of a player not being processed by the proxy
                .build();

        AwaitingKickTask awaitedKickTask = new AwaitingKickTask(
                this.componentAdapter,
                this.awaitedKickTaskCache,
                this.getProxy()
        );
        this.schedule(awaitedKickTask, 0, 150, TimeUnit.MILLISECONDS);

        //this.getProxy().registerChannel(MessageReceiver.CHANNEL);
        this.getProxy().getPluginManager().registerListener(this, this.brandCheckProcessor);
        this.getProxy().getPluginManager().registerListener(this, this.playerSettingsProcessor);
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this, coreInstance));

        BungeeCommandManager<CommandSender> commandManager = new BungeeCommandManager<>(
                this,
                ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.identity()
        );

        AnnotationParser<CommandSender> commandParser = new AnnotationParser<>(commandManager, CommandSender.class);
        commandParser.parse(List.of(
                new ConnectorCommand(this),
                new WhitelistCommand(this),
                new BlacklistCommand(this)
        ));

        getLogger().info("(Bungee) Enabling... [DONE]");
    }

    @Override
    public void onDisable() {
        getLogger().info("(Bungee) Disabling...");
        getLogger().info("(Bungee) Disconnecting from the backend...");

        v4GuardConnectorProvider.unregister();

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

        if (!player.getName().equals(playerName)) {
            UnifiedLogger.get().log(Level.WARNING,
                    "[FND] Player " + playerName + " is not equal to the player's name "
                            + player.getName()
                            + ". This should not be happening, this is UEB from the backend!"
            );
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
    public void kickPlayer(String playerName, List<String> reason) {
        kickPlayer(playerName, reason, false);
    }

    public void kickPlayer(String playerName, List<String> reason, boolean later) {
        if (later) {
            awaitedKickTaskCache.put(playerName, new AwaitingKick<>(playerName, reason));
            return;
        }

        PlayerFetchResult<ProxiedPlayer> fetchedPlayer = fetchPlayer(playerName);

        if (!fetchedPlayer.isOnline()) {
            return;
        }

        if (!fetchedPlayer.getPlayer().getName().equals(playerName)) {
            UnifiedLogger.get().log(Level.WARNING,
                    "[SYNC] Player " + playerName + " is not equal to the player's name "
                            + fetchedPlayer.getPlayer().getName() + " removing await kick task. And notify us, " +
                            "this is UEB from the backend!"
            );
            return;
        }

        ProxiedPlayer player = fetchedPlayer.getPlayer();
        player.disconnect(this.getComponentAdapter().adapt(reason, ((UserConnection) player).getCh().getEncodeVersion() < 735));
        awaitedKickTaskCache.invalidate(playerName);
    }

    @Override
    public UniversalTask schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return new BungeeTask(getProxy().getScheduler().schedule(this, runnable, delay, period, timeUnit));
    }

    @Override
    public ComponentAdapter<BaseComponent[]> getComponentAdapter() {
        return this.componentAdapter;
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
