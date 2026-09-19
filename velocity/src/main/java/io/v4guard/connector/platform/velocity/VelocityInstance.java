package io.v4guard.connector.platform.velocity;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.Scheduler;
import io.v4guard.connector.api.v4GuardConnectorProvider;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.cache.CheckDataCache;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;
import io.v4guard.connector.common.compatibility.*;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import io.v4guard.connector.platform.velocity.adapter.VelocityComponentAdapter;
import io.v4guard.connector.platform.velocity.adapter.VelocityMessenger;
import io.v4guard.connector.platform.velocity.check.VelocityCheckProcessor;
import io.v4guard.connector.platform.velocity.command.ConnectorCommand;
import io.v4guard.connector.platform.velocity.command.sub.BlacklistCommand;
import io.v4guard.connector.platform.velocity.command.sub.WhitelistCommand;
import io.v4guard.connector.platform.velocity.listener.PlayerListener;
import io.v4guard.connector.platform.velocity.listener.PlayerSettingsListener;
import io.v4guard.connector.platform.velocity.listener.PluginMessagingListener;
import io.v4guard.connector.platform.velocity.task.AwaitingKickTask;

import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.core.lookup.SystemPropertiesLookup;
import org.bstats.velocity.Metrics;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Plugin(
        id = "v4guard-plugin",
        name = "v4Guard Plugin",
        version = CoreInstance.PLUGIN_VERSION,
        url = "https://v4guard.io",
        description = "v4Guard Plugin for Minecraft Servers",
        authors = {"v4Guard"}

)
public class VelocityInstance implements UniversalPlugin {

    private static VelocityInstance instance;
    private CoreInstance coreInstance;
    private final ProxyServer server;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private final Path dataDirectory;
    private final PluginDescription pluginDescription;
    private VelocityMessenger messenger;
    private CheckDataCache checkDataCache;
    private VelocityCheckProcessor checkProcessor;
    private PluginMessagingListener brandCheckProcessor;
    private PlayerSettingsListener playerSettingsProcessor;

    private ComponentAdapter<Component> componentAdapter;


    @Inject
    public VelocityInstance(
            ProxyServer server
            , Logger logger
            , Metrics.Factory metricsFactory
            , @DataDirectory Path dataDirectory
            , PluginDescription pluginDescription
    ) {
        instance = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.pluginDescription = pluginDescription;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.logger.info("(Velocity) Enabling...");

        try {
            metricsFactory.make(this, 16220);
        } catch (Exception ex) {
            this.logger.warning("(Velocity) Failed to connect with bStats [WARN]");
        }

        this.componentAdapter = new VelocityComponentAdapter();

        this.checkProcessor = new VelocityCheckProcessor(this);
        this.brandCheckProcessor = new PluginMessagingListener();
        this.playerSettingsProcessor = new PlayerSettingsListener();
        this.messenger = new VelocityMessenger(this.server, this.componentAdapter);
        this.checkDataCache = new CheckDataCache();

        try {
            coreInstance = new CoreInstance(ServerPlatform.VELOCITY, this);
            coreInstance.initialize();
        } catch (Exception exception) {
            this.logger.log(Level.SEVERE, "(Velocity) Enabling... [ERROR]", exception);
            return;
        }

        for (String channel : BrandCheckProcessor.MODERN_LABYMOD_CHANNELS) {
            this.server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(channel));
        }

        this.server.getChannelRegistrar().register(new LegacyChannelIdentifier(BrandCheckProcessor.LEGACY_LABYMOD_CHANNEL));

        this.server.getEventManager().register(this, this.brandCheckProcessor);
        this.server.getEventManager().register(this, this.playerSettingsProcessor);
        this.server.getEventManager().register(this, new PlayerListener(this));

        int connectionTimeout = System.getProperty("io.v4guard.connector.connectionTimeout") != null
                ? Integer.parseInt(System.getProperty("io.v4guard.connector.connectionTimeout", "5000"))
                : this.server.getConfiguration().getConnectTimeout();

        if (connectionTimeout < 3000) {
            this.logger.warning("(Velocity) Connect timeout is lower than 3000ms, forcing our own timeout of 5000ms. You should raise the timeout in your velocity config.");
            connectionTimeout = 5000;
        }

        // Prevents memory leaks as we don't want to keep the player in the cache after the connection has been timed out
        this.awaitedKickTaskCache = Caffeine.newBuilder()
                .expireAfterWrite(connectionTimeout, TimeUnit.MILLISECONDS)
                .build();

        schedule(new AwaitingKickTask(this.awaitedKickTaskCache, this.server, this.componentAdapter), 0, 150, TimeUnit.MILLISECONDS);

        VelocityCommandManager<CommandSource> commandManager = new VelocityCommandManager<>(
                this.server.getPluginManager().getPlugin("v4guard-plugin").orElseThrow(),
                this.server, ExecutionCoordinator.asyncCoordinator(),
                SenderMapper.identity()
        );

        AnnotationParser<CommandSource> commandParser = new AnnotationParser<>(commandManager, CommandSource.class);
        commandParser.parse(List.of(
                new ConnectorCommand(this),
                new WhitelistCommand(this),
                new BlacklistCommand(this)
        ));

        this.logger.info("(Velocity) Enabling... [DONE]");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("(Velocity) Disabling...");
        this.logger.info("(Velocity) Disconnecting from the backend...");

        server.getScheduler().tasksByPlugin(this).forEach(ScheduledTask::cancel);

        v4GuardConnectorProvider.unregister();

        try {
            coreInstance.getRemoteConnection().disconnect();
        } catch (Exception exception) {
            this.logger.log(Level.SEVERE, "(Velocity) Disabling... [ERROR]", exception);
            return;
        }

        this.logger.info("(Velocity) Disabling... [DONE]");
    }

    public static VelocityInstance get() {
        return instance;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getPluginName() {
        return pluginDescription.getName().orElse(pluginDescription.getId());
    }

    @Override
    public boolean isPluginEnabled(String pluginName) {
        return this.server.getPluginManager().isLoaded(pluginName);
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public PlayerFetchResult<Player> fetchPlayer(String playerName) {
        Optional<Player> player = this.server.getPlayer(playerName);
        if (player.isEmpty()) {
            return new PlayerFetchResult<>(null, null, false);
        }

        Player foundedPlayer = player.get();

        if (!foundedPlayer.getUsername().equals(playerName)) {
            UnifiedLogger.get().log(Level.WARNING,
                    "[FND] Player " + playerName + " is not equal to the player's name "
                            + foundedPlayer.getUsername()
                            + ". This should not be happening, this is UEB from the backend!"
            );
            return new PlayerFetchResult<>(null, null, false);
        }

        Optional<ServerConnection> server = foundedPlayer.getCurrentServer();

        return new PlayerFetchResult<>(
                foundedPlayer
                , server.map(serverCon -> serverCon.getServerInfo().getName()).orElse(null)
                , true
        );
    }

    @Override
    public void kickPlayer(String playerName, List<String> reason) {
        kickPlayer(playerName, reason, false);
    }

    @Override
    public void kickPlayer(String playerName, List<String> reason, boolean later) {
        if (later) {
            awaitedKickTaskCache.put(playerName, new AwaitingKick<>(playerName, reason));
            return;
        }

        PlayerFetchResult<Player> fetchedPlayer = fetchPlayer(playerName);

        if (!fetchedPlayer.isOnline()) {
            return;
        }

        Player player = fetchedPlayer.getPlayer();

        if (!player.getUsername().equals(playerName)) {
            UnifiedLogger.get().log(Level.WARNING,
                    "[SYNC] Player " + playerName + " is not equal to the player's name "
                            + player.getUsername() + " removing await kick task. And notify us, " +
                            "this is UEB from the backend!"
            );
            return;
        }

        Component component = this.componentAdapter.adapt(reason);
        player.disconnect(component != null ? component : Component.empty());
    }

    @Override
    public UniversalTask schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        Scheduler.TaskBuilder taskBuilder = server.getScheduler()
                .buildTask(this, runnable)
                .delay(delay, timeUnit)
                .repeat(period, timeUnit);

        return new VelocityTask(taskBuilder.schedule());
    }

    @Override
    public ComponentAdapter<Component> getComponentAdapter() {
        return componentAdapter;
    }

    @Override
    public VelocityMessenger getMessenger() {
        return messenger;
    }

    @Override
    public CheckDataCache getCheckDataCache() {
        return checkDataCache;
    }


    @Override
    public VelocityCheckProcessor getCheckProcessor() {
        return checkProcessor;
    }

    @Override
    public BrandCheckProcessor getBrandCheckProcessor() {
        return this.brandCheckProcessor;
    }

    @Override
    public PlayerSettingsCheckProcessor getPlayerSettingsCheckProcessor() {
        return playerSettingsProcessor;
    }
}
