package io.v4guard.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import io.v4guard.plugin.core.accounts.AccountShieldManager;
import io.v4guard.plugin.core.mode.v4GuardMode;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.velocity.accounts.VelocityMessageReceiver;
import io.v4guard.plugin.velocity.listener.AntiVPNListener;
import io.v4guard.plugin.velocity.messager.Messager;
import net.kyori.adventure.text.Component;
import org.bstats.velocity.Metrics;

import java.util.logging.Logger;

@Plugin(
        id = "v4guard-plugin",
        name = "v4Guard Plugin",
        version = v4GuardCore.pluginVersion,
        url = "https://v4guard.io",
        description = "v4Guard Plugin for Minecraft Servers",
        authors = {"DigitalSynware"},
        dependencies = {
                @Dependency(id = "v4guard-account-shield", optional = true)
        }

)
public class v4GuardVelocity {

    private static v4GuardCore core;

    private static v4GuardVelocity v4GuardVelocity;
    private final Metrics.Factory metricsFactory;
    private ProxyServer server;
    private Logger logger;
    private Messager messager;

    @Inject
    public v4GuardVelocity(ProxyServer server, Logger logger, Metrics.Factory metricsFactory) {
        v4GuardVelocity = this;
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getConsoleCommandSource().sendMessage(Component.text("§e[v4guard-plugin] (Velocity) Enabling..."));
        try {
            metricsFactory.make(this, 16220);
        } catch (Exception ex) {
            server.getConsoleCommandSource().sendMessage(Component.text("§e[v4guard-plugin] (Velocity) Failed to connect with bStats [WARN]"));
        }
        try {
            core = new v4GuardCore(v4GuardMode.VELOCITY);
            core.getCheckManager().addProcessor(new VelocityCheckProcessor());
            core.setAccountShieldManager(new AccountShieldManager(new VelocityMessageReceiver(this)));
        } catch (Exception e) {
            server.getConsoleCommandSource().sendMessage(Component.text("§c[v4guard-plugin] (Velocity) Enabling... [ERROR]"));
            server.getConsoleCommandSource().sendMessage(Component.text("§cPlease check the console for more information and report this error."));
            e.printStackTrace();
            return;
        }
        v4GuardVelocity = this;
        server.getEventManager().register(this, new AntiVPNListener());
        server.getConsoleCommandSource().sendMessage(Component.text("§e[v4guard-plugin] (Velocity) Enabling... [DONE]"));
        this.messager = new Messager();
        getCoreInstance().setAccountShieldFound(this.getServer().getPluginManager().isLoaded("v4guard-account-shield"));
    }

    public ProxyServer getServer() {
        return server;
    }

    public Messager getMessager() {
        return messager;
    }

    public static v4GuardVelocity getV4Guard() {
        return v4GuardVelocity;
    }

    public static v4GuardCore getCoreInstance() {
        return core;
    }
}
