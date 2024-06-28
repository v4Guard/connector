package io.v4guard.connector.platform.velocity;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
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
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.accounts.MessageReceiver;
import io.v4guard.connector.common.cache.CheckDataCache;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;
import io.v4guard.connector.common.compatibility.PlayerFetchResult;
import io.v4guard.connector.common.compatibility.ServerPlatform;
import io.v4guard.connector.common.compatibility.UniversalPlugin;
import io.v4guard.connector.common.compatibility.UniversalTask;
import io.v4guard.connector.common.constants.ShieldChannels;
import io.v4guard.connector.platform.velocity.accounts.VelocityMessageReceiver;
import io.v4guard.connector.platform.velocity.adapter.VelocityMessenger;
import io.v4guard.connector.platform.velocity.check.VelocityCheckProcessor;
import io.v4guard.connector.platform.velocity.listener.PlayerListener;
import io.v4guard.connector.platform.velocity.listener.PlayerSettingsListener;
import io.v4guard.connector.platform.velocity.listener.PluginMessagingListener;
import io.v4guard.connector.platform.velocity.task.AwaitedKickTask;
import net.kyori.adventure.text.Component;
import org.bstats.velocity.Metrics;

import java.io.File;
import java.nio.file.Path;
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
    private Cache<Player, String> awaitedKickTaskCache;
    private final Path dataDirectory;
    private final PluginDescription pluginDescription;
    private VelocityMessenger messenger;
    private CheckDataCache checkDataCache;
    private MessageReceiver messageReceiver;
    private VelocityCheckProcessor checkProcessor;
    private PluginMessagingListener brandCheckProcessor;
    private PlayerSettingsListener playerSettingsProcessor;
    private boolean floodGateFound;

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

        this.checkProcessor = new VelocityCheckProcessor(this);
        this.brandCheckProcessor = new PluginMessagingListener();
        this.playerSettingsProcessor = new PlayerSettingsListener();
        this.messenger = new VelocityMessenger();
        this.checkDataCache = new CheckDataCache();
        this.messageReceiver = new VelocityMessageReceiver();

        try {
            coreInstance = new CoreInstance(ServerPlatform.VELOCITY, this);
            coreInstance.initialize();
        } catch (Exception exception) {
            this.logger.log(Level.SEVERE, "(Velocity) Enabling... [ERROR]", exception);
            return;
        }

        if (isPluginEnabled("floodgate")) {
            floodGateFound = true;
        }

        //TODO: Why is legacy channel needs to be registered, it's identical to modern.
        this.server.getChannelRegistrar().register(new LegacyChannelIdentifier(ShieldChannels.VELOCITY_CHANNEL));
        this.server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(ShieldChannels.VELOCITY_CHANNEL));

        for (String channel : BrandCheckProcessor.MODERN_LABYMOD_CHANNELS) {
            this.server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(channel));
        }

        this.server.getChannelRegistrar().register(new LegacyChannelIdentifier(BrandCheckProcessor.LEGACY_LABYMOD_CHANNEL));

        this.server.getEventManager().register(this, this.messageReceiver);
        this.server.getEventManager().register(this, this.brandCheckProcessor);
        this.server.getEventManager().register(this, this.playerSettingsProcessor);
        this.server.getEventManager().register(this, new PlayerListener(this));

        this.awaitedKickTaskCache = Caffeine
                .newBuilder()
                .build();

        schedule(new AwaitedKickTask(this.awaitedKickTaskCache), 0, 150, TimeUnit.MILLISECONDS);

        this.logger.info("(Velocity) Enabling... [DONE]");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("(Velocity) Disabling...");
        this.logger.info("(Velocity) Disconnecting from the backend...");

        server.getScheduler().tasksByPlugin(this).forEach(ScheduledTask::cancel);

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

        Optional<ServerConnection> server = player.get().getCurrentServer();

        return new PlayerFetchResult<>(
                player.get()
                , server.map(serverConnection -> serverConnection.getServerInfo().getName()).orElse(null)
                , true
        );
    }

    @Override
    public void kickPlayer(String playerName, String reason) {
        PlayerFetchResult<Player> fetchedPlayer = fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            awaitedKickTaskCache.put(fetchedPlayer.getPlayer(), reason);
        }
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
    public VelocityMessenger getMessenger() {
        return messenger;
    }

    @Override
    public CheckDataCache getCheckDataCache() {
        return checkDataCache;
    }

    @Override
    public MessageReceiver getMessageReceiver() {
        return messageReceiver;
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

    @Override
    public boolean isFloodgatePresent() {
        return floodGateFound;
    }

    public Cache<Player, String> getAwaitedKickTaskCache() {
        return awaitedKickTaskCache;
    }

}
