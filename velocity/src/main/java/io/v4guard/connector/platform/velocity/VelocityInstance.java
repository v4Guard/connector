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
import io.v4guard.connector.api.v4GuardConnectorProvider;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.cache.CheckDataCache;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;
import io.v4guard.connector.common.compatibility.*;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import io.v4guard.connector.platform.velocity.adapter.VelocityMessenger;
import io.v4guard.connector.platform.velocity.check.VelocityCheckProcessor;
import io.v4guard.connector.platform.velocity.command.ConnectorCommand;
import io.v4guard.connector.common.command.internal.annotations.CommandFlag;
import io.v4guard.connector.common.command.internal.modifier.ValueCommandFlagModifier;
import io.v4guard.connector.common.command.internal.part.FlagPartFactory;
import io.v4guard.connector.common.command.internal.usage.CustomUsageBuilder;
import io.v4guard.connector.platform.velocity.listener.PlayerListener;
import io.v4guard.connector.platform.velocity.listener.PlayerSettingsListener;
import io.v4guard.connector.platform.velocity.listener.PluginMessagingListener;
import io.v4guard.connector.platform.velocity.task.AwaitingKickTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import team.unnamed.commandflow.CommandManager;
import team.unnamed.commandflow.annotated.AnnotatedCommandTreeBuilder;
import team.unnamed.commandflow.annotated.SubCommandInstanceCreator;
import team.unnamed.commandflow.annotated.part.Key;
import team.unnamed.commandflow.annotated.part.PartInjector;
import team.unnamed.commandflow.annotated.part.defaults.DefaultsModule;
import team.unnamed.commandflow.velocity.VelocityCommandManager;
import team.unnamed.commandflow.velocity.factory.VelocityModule;

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
    private Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private final Path dataDirectory;
    private final PluginDescription pluginDescription;
    private VelocityMessenger messenger;
    private CheckDataCache checkDataCache;
    private VelocityCheckProcessor checkProcessor;
    private PluginMessagingListener brandCheckProcessor;
    private PlayerSettingsListener playerSettingsProcessor;
    private final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer
            .builder()
            .character('§')
            .hexColors()
            .hexCharacter('#')
            .build();


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
        this.messenger = new VelocityMessenger(this);
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

        int connectionTimeout = this.server.getConfiguration().getConnectTimeout();
        if (connectionTimeout < 3000) {
            this.logger.warning("(Velocity) Connect timeout is lower than 3000ms, forcing our own timeout of 5000ms. You should raise the timeout in your velocity config.");
            connectionTimeout = 5000;
        }

        // Prevents memory leaks as we don't want to keep the player in the cache after the connection has been timed out
        this.awaitedKickTaskCache = Caffeine.newBuilder()
                .expireAfterWrite(connectionTimeout, TimeUnit.MILLISECONDS)
                .build();

        schedule(new AwaitingKickTask(this.awaitedKickTaskCache, this.server), 0, 150, TimeUnit.MILLISECONDS);

        PartInjector partInjector = PartInjector.create();
        partInjector.install(new DefaultsModule());
        partInjector.install(new VelocityModule(server));
        partInjector.bindFactory(new Key(Boolean.class, CommandFlag.class), new FlagPartFactory());
        partInjector.bindModifier(CommandFlag.class, new ValueCommandFlagModifier());

        AnnotatedCommandTreeBuilder builder = getAnnotatedCommandTreeBuilder(partInjector);

        CommandManager commandManager = new VelocityCommandManager(server, this);

        commandManager.setUsageBuilder(new CustomUsageBuilder());

        commandManager.registerCommands(builder.fromClass(new ConnectorCommand(this)));

        this.logger.info("(Velocity) Enabling... [DONE]");
    }

    private @NotNull AnnotatedCommandTreeBuilder getAnnotatedCommandTreeBuilder(PartInjector partInjector) {
        SubCommandInstanceCreator subCommandInstanceCreator = (aClass, commandClass) -> {
            try {
                return aClass.getConstructor(VelocityInstance.class).newInstance(this);
            } catch (Exception e) {
                UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while registering the commands", e);
            }
            return null;
        };

        return AnnotatedCommandTreeBuilder.create(partInjector, subCommandInstanceCreator);
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
    public void kickPlayer(String playerName, String reason) {
        kickPlayer(playerName, reason, false);
    }

    @Override
    public void kickPlayer(String playerName, String reason, boolean later) {
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

        player.disconnect(Component.text(reason));
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

    public LegacyComponentSerializer getLegacyComponentSerializer() {
        return legacyComponentSerializer;
    }
}
