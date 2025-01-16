package io.v4guard.connector.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.v4guard.connector.common.accounts.AccountShieldSender;
import io.v4guard.connector.common.cache.CacheTicker;
import io.v4guard.connector.common.cache.CheckDataCache;
import io.v4guard.connector.common.check.PendingTasks;
import io.v4guard.connector.common.compatibility.ServerPlatform;
import io.v4guard.connector.common.compatibility.UniversalPlugin;
import io.v4guard.connector.common.socket.ActiveSettings;
import io.v4guard.connector.common.socket.Connection;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreInstance {

    public static final String PLUGIN_VERSION = "2.5.0";

    private static CoreInstance instance;
    private PendingTasks pendingTasks;
    private Connection remoteConnection;
    private AccountShieldSender accountShieldSender;
    private ObjectMapper objectMapper;
    private ActiveSettings activeSettings;

    private boolean debugEnabled;
    private boolean accountShieldFound;
    private boolean floodgateFound;
    private final ServerPlatform platform;
    private final UniversalPlugin plugin;

    public CoreInstance(ServerPlatform platform, UniversalPlugin plugin) {
        instance = this;
        this.platform = platform;
        this.plugin = plugin;
    }

    public void initialize() {
        initializeLogger();
        String debugProperty = System.getProperty("v4guardDebug", "false");

        this.objectMapper = new ObjectMapper();
        this.debugEnabled = Boolean.parseBoolean(debugProperty);

        if (debugEnabled) {
            UnifiedLogger.get().log(Level.WARNING, "Debugging mode has been activated via java arguments (Dv4guardDebug=" + debugProperty + ")");
        }

        this.pendingTasks = new PendingTasks();
        this.remoteConnection = new Connection(this);
        this.accountShieldFound = this.plugin.isPluginEnabled("v4guard-account-shield");
        this.floodgateFound = this.plugin.isPluginEnabled("floodgate");
        this.accountShieldSender = new AccountShieldSender(this);

        this.remoteConnection.prepareAndConnect();
        this.plugin.schedule(new CacheTicker(this), 0, 100, TimeUnit.MILLISECONDS);
    }

    public void initializeLogger() {
        Logger logger = Logger.getLogger(plugin.getPluginName());
        logger.setUseParentHandlers(true);

        UnifiedLogger.overrideBy(logger);
    }

    public boolean isAccountShieldFound() {
        return this.accountShieldFound;
    }

    public void setAccountShieldFound(boolean accountShieldFound) {
        this.accountShieldFound = accountShieldFound;
    }

    public boolean isFloodgateFound() {
        return this.floodgateFound;
    }

    public AccountShieldSender getAccountShieldSender() {
        return this.accountShieldSender;
    }

    public UniversalPlugin getPlugin() {
        return plugin;
    }

    public static CoreInstance get() {
        return instance;
    }

    public ServerPlatform getPlatform() {
        return platform;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public Connection getRemoteConnection() {
        return remoteConnection;
    }

    public PendingTasks getPendingTasks() {
        return pendingTasks;
    }

    public CheckDataCache getCheckDataCache() {
        return plugin.getCheckDataCache();
    }

    public ActiveSettings getActiveSettings() {
        return activeSettings;
    }

    public void setActiveSettings(ActiveSettings activeSettings) {
        this.activeSettings = activeSettings;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            UnifiedLogger.get().log(Level.SEVERE, "Could not read json tree", e);
            return null;
        }
    }
}
