package io.v4guard.connector.platform.bungee;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.v4guard.connector.api.v4GuardConnectorProvider;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;
import io.v4guard.connector.common.command.internal.annotations.CommandFlag;
import io.v4guard.connector.common.command.internal.modifier.ValueCommandFlagModifier;
import io.v4guard.connector.common.command.internal.part.FlagPartFactory;
import io.v4guard.connector.common.command.internal.usage.CustomUsageBuilder;
import io.v4guard.connector.common.compatibility.PlayerFetchResult;
import io.v4guard.connector.common.compatibility.ServerPlatform;
import io.v4guard.connector.common.compatibility.UniversalPlugin;
import io.v4guard.connector.common.compatibility.UniversalTask;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import io.v4guard.connector.platform.bungee.adapter.BungeeMessenger;
import io.v4guard.connector.platform.bungee.cache.BungeeCheckDataCache;
import io.v4guard.connector.platform.bungee.check.BungeeCheckProcessor;
import io.v4guard.connector.platform.bungee.command.ConnectorCommand;
import io.v4guard.connector.platform.bungee.listener.PlayerListener;
import io.v4guard.connector.platform.bungee.listener.PlayerSettingsListener;
import io.v4guard.connector.platform.bungee.listener.PluginMessagingListener;
import io.v4guard.connector.platform.bungee.task.AwaitingKickTask;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import team.unnamed.commandflow.CommandManager;
import team.unnamed.commandflow.annotated.AnnotatedCommandTreeBuilder;
import team.unnamed.commandflow.annotated.SubCommandInstanceCreator;
import team.unnamed.commandflow.annotated.part.Key;
import team.unnamed.commandflow.annotated.part.PartInjector;
import team.unnamed.commandflow.annotated.part.defaults.DefaultsModule;
import team.unnamed.commandflow.bungee.BungeeCommandManager;
import team.unnamed.commandflow.bungee.factory.BungeeModule;

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
    private AwaitingKickTask awaitedKickTask;
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


        this.awaitedKickTaskCache = Caffeine
                .newBuilder()
                .expireAfterWrite(ProxyServer.getInstance().getConfig().getTimeout(), TimeUnit.MILLISECONDS) //prevent memory leaks in case of a player not being processed by the proxy
                .build();


        this.awaitedKickTask = new AwaitingKickTask(this.awaitedKickTaskCache, this.getProxy());
        this.schedule(this.awaitedKickTask, 0, 150, TimeUnit.MILLISECONDS);

        //this.getProxy().registerChannel(MessageReceiver.CHANNEL);
        this.getProxy().getPluginManager().registerListener(this, this.brandCheckProcessor);
        this.getProxy().getPluginManager().registerListener(this, this.playerSettingsProcessor);
        this.getProxy().getPluginManager().registerListener(this, new PlayerListener(this, coreInstance));

        PartInjector partInjector = PartInjector.create();
        partInjector.install(new DefaultsModule());
        partInjector.install(new BungeeModule());
        partInjector.bindFactory(new Key(Boolean.class, CommandFlag.class), new FlagPartFactory());
        partInjector.bindModifier(CommandFlag.class, new ValueCommandFlagModifier());


        SubCommandInstanceCreator subCommandInstanceCreator = (aClass, commandClass) -> {
            try {
                return aClass.getConstructor().newInstance();
            } catch (Exception e) {
                UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while registering the commands", e);
            }
            return null;
        };

        AnnotatedCommandTreeBuilder builder = AnnotatedCommandTreeBuilder.create(partInjector, subCommandInstanceCreator);

        CommandManager commandManager = new BungeeCommandManager(this);

        commandManager.setUsageBuilder(new CustomUsageBuilder());

        commandManager.registerCommands(builder.fromClass(new ConnectorCommand()));


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
        if (later) {
            awaitedKickTaskCache.put(playerName, new AwaitingKick<>(playerName, reason));
            return;
        }

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
