package io.v4guard.connector.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.v4guard.connector.api.ConnectorAPI;
import io.v4guard.connector.api.socket.EventRegistry;
import io.v4guard.connector.api.v4GuardConnectorProvider;
import io.v4guard.connector.common.api.DefaultConnectorAPI;
import io.v4guard.connector.common.cache.CacheTicker;
import io.v4guard.connector.common.cache.CheckDataCache;
import io.v4guard.connector.common.check.PendingTasks;
import io.v4guard.connector.common.compatibility.ServerPlatform;
import io.v4guard.connector.common.compatibility.UniversalPlugin;
import io.v4guard.connector.common.socket.settings.DefaultActiveSettings;
import io.v4guard.connector.common.socket.ActiveConnection;
import io.v4guard.connector.common.socket.registry.DefaultEventRegistry;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreInstance {

    public static final String PLUGIN_VERSION = "2.5.0";

    private static CoreInstance instance;
    private PendingTasks pendingTasks;
    private ActiveConnection remoteActiveConnection;
    private ObjectMapper objectMapper;
    private DefaultActiveSettings defaultActiveSettings;
    private EventRegistry eventRegistry;

    private boolean debugEnabled;
    private boolean floodgateFound;
    private final ServerPlatform platform;
    private final UniversalPlugin plugin;
    private final ConnectorAPI connectorAPI;

    public CoreInstance(ServerPlatform platform, UniversalPlugin plugin) {
        instance = this;
        this.platform = platform;
        this.plugin = plugin;

        this.connectorAPI = new DefaultConnectorAPI();

        v4GuardConnectorProvider.register(this.connectorAPI);
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
        this.remoteActiveConnection = new ActiveConnection(this);
        this.floodgateFound = this.plugin.isPluginEnabled("floodgate");

        this.remoteActiveConnection.prepareAndConnect();
        this.eventRegistry = new DefaultEventRegistry();
        this.plugin.schedule(new CacheTicker(this), 0, 100, TimeUnit.MILLISECONDS);
    }

    public void initializeLogger() {
        Logger logger = Logger.getLogger(plugin.getPluginName());
        logger.setUseParentHandlers(true);

        UnifiedLogger.overrideBy(logger);
    }

    public boolean isFloodgateFound() {
        return this.floodgateFound;
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

    public ActiveConnection getRemoteConnection() {
        return remoteActiveConnection;
    }

    public PendingTasks getPendingTasks() {
        return pendingTasks;
    }

    public CheckDataCache getCheckDataCache() {
        return plugin.getCheckDataCache();
    }

    public DefaultActiveSettings getActiveSettings() {
        return defaultActiveSettings;
    }

    public void setActiveSettings(DefaultActiveSettings defaultActiveSettings) {
        this.defaultActiveSettings = defaultActiveSettings;
        this.connectorAPI.setActiveSettings(defaultActiveSettings);
    }

    public EventRegistry getEventRegistry() {
        return eventRegistry;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public ConnectorAPI getConnectorAPI() {
        return connectorAPI;
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
