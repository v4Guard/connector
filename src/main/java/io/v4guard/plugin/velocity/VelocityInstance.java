package io.v4guard.plugin.velocity;

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
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.accounts.MessageReceiver;
import io.v4guard.plugin.core.check.brand.BrandCheckProcessor;
import io.v4guard.plugin.core.compatibility.PlayerFetchResult;
import io.v4guard.plugin.core.compatibility.ServerPlatform;
import io.v4guard.plugin.core.compatibility.UniversalPlugin;
import io.v4guard.plugin.core.constants.ShieldChannels;
import io.v4guard.plugin.velocity.accounts.VelocityMessageReceiver;
import io.v4guard.plugin.velocity.adapter.VelocityMessenger;
import io.v4guard.plugin.velocity.check.VelocityCheckProcessor;
import io.v4guard.plugin.velocity.listener.AntiVPNListener;
import io.v4guard.plugin.velocity.listener.PluginMessagingListener;
import net.kyori.adventure.text.Component;
import org.bstats.velocity.Metrics;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    private ProxyServer server;
    private Logger logger;
    private Metrics.Factory metricsFactory;
    private Path dataDirectory;
    private PluginDescription pluginDescription;
    private ConcurrentHashMap<Integer, ScheduledTask> idToTaskMap;
    private AtomicInteger nextTaskId;
    private VelocityMessenger messenger;
    private MessageReceiver messageReceiver;
    private VelocityCheckProcessor checkProcessor;
    private PluginMessagingListener brandCheckProcessor;

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
        this.idToTaskMap = new ConcurrentHashMap<>();
        this.nextTaskId = new AtomicInteger(0);
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

        try {
            coreInstance = new CoreInstance(ServerPlatform.VELOCITY, this);
            coreInstance.initialize();
        } catch (Exception exception) {
            this.logger.log(Level.SEVERE, "(Velocity) Enabling... [ERROR]", exception);
            return;
        }

        this.checkProcessor = new VelocityCheckProcessor(this);
        this.brandCheckProcessor = new PluginMessagingListener();
        this.messenger = new VelocityMessenger();
        this.messageReceiver = new VelocityMessageReceiver();

        //TODO: Why is legacy channel needs to be registered, it's identical to modern.
        this.server.getChannelRegistrar().register(new LegacyChannelIdentifier(ShieldChannels.VELOCITY_CHANNEL));
        this.server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(ShieldChannels.VELOCITY_CHANNEL));
        this.server.getEventManager().register(this, this.messageReceiver);
        this.server.getEventManager().register(this, this.brandCheckProcessor);
        this.server.getEventManager().register(this, new AntiVPNListener());

        this.logger.info("(Velocity) Enabling... [DONE]");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.logger.info("(Velocity) Disabling...");
        this.logger.info("(Velocity) Disconnecting from the backend...");

        try {
            coreInstance.getBackend().getSocket().disconnect();
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
    public String getPlayerServer(String playerName) {
        PlayerFetchResult<Player> fetchedPlayer = fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            Optional<ServerConnection> server = fetchedPlayer.getPlayer().getCurrentServer();

            if (server.isPresent()) {
                return server.get().getServerInfo().getName();
            }
        }

        return null;
    }

    @Override
    public PlayerFetchResult<Player> fetchPlayer(String playerName) {
        Optional<Player> player = this.server.getPlayer(playerName);

        return new PlayerFetchResult<>(player.orElse(null), player.isPresent());
    }

    @Override
    public void kickPlayer(String playerName, String reason) {
        PlayerFetchResult<Player> fetchedPlayer = fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            fetchedPlayer.getPlayer().disconnect(Component.text(reason));
        }
    }

    @Override
    public VelocityMessenger getMessenger() {
        return messenger;
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
    public int schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        int taskId = nextTaskId.getAndIncrement();
        Scheduler.TaskBuilder taskBuilder = server.getScheduler()
                .buildTask(this, runnable)
                .delay(delay, timeUnit)
                .repeat(period, timeUnit);

        idToTaskMap.put(taskId, taskBuilder.schedule());

        return taskId;
    }

    @Override
    public void cancelTask(int taskId) {
        idToTaskMap.remove(taskId).cancel();
    }
}
