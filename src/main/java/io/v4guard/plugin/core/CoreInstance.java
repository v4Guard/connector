package io.v4guard.plugin.core;

import io.v4guard.plugin.core.accounts.AccountShieldSender;
import io.v4guard.plugin.core.cache.CacheTicker;
import io.v4guard.plugin.core.cache.CheckDataCache;
import io.v4guard.plugin.core.check.PendingTasks;
import io.v4guard.plugin.core.compatibility.ServerPlatform;
import io.v4guard.plugin.core.compatibility.UniversalPlugin;
import io.v4guard.plugin.core.socket.Backend;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreInstance {

    public static final String PLUGIN_VERSION = "2.0.0";

    private static CoreInstance instance;

    private PendingTasks pendingTasks;

    private Backend backend;

    private AccountShieldSender accountShieldSender;

    private boolean debugEnabled;
    private boolean accountShieldFound;
    private ServerPlatform platform;
    private UniversalPlugin plugin;

    public CoreInstance(ServerPlatform platform, UniversalPlugin plugin) {
        instance = this;
        this.platform = platform;
        this.plugin = plugin;
    }

    public void initialize() {
        initializeLogger();
        String debugProperty = System.getProperty("v4guardDebug", "false");

        this.debugEnabled = Boolean.parseBoolean(debugProperty);

        if(debugEnabled) {
           UnifiedLogger.get().log(Level.WARNING,"Debugging mode has been activated via java arguments (Dv4guardDebug=" + debugProperty + ")");
        }

        this.pendingTasks = new PendingTasks();
        this.backend = new Backend(this);
        this.accountShieldFound = this.plugin.isPluginEnabled("v4guard-account-shield");
        this.accountShieldSender = new AccountShieldSender();

        this.backend.prepareAndConnect();
        this.plugin.schedule(new CacheTicker(), 0, 100, TimeUnit.MILLISECONDS);
    }

    public void initializeLogger() {
        Logger logger = Logger.getLogger(plugin.getPluginName());
        logger.setUseParentHandlers(true);

        UnifiedLogger.overrideBy(logger);
    }

    public boolean isAccountShieldFound() {
        return this.accountShieldFound;
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

    public Backend getBackend() {
        return backend;
    }

    public PendingTasks getPendingTasks() {
        return pendingTasks;
    }

    public CheckDataCache getCheckDataCache() {
        return plugin.getCheckDataCache();
    }
}
