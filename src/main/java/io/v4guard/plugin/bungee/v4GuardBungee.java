package io.v4guard.plugin.bungee;

import io.v4guard.plugin.bungee.listener.AntiVPNListener;
import io.v4guard.plugin.bungee.messager.Messager;
import io.v4guard.plugin.core.mode.v4GuardMode;
import io.v4guard.plugin.core.v4GuardCore;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public class v4GuardBungee extends Plugin {

    private static v4GuardCore core;
    private static v4GuardBungee v4GuardBungee;
    private Messager messager;

    @Override
    public void onEnable(){
        this.getProxy().getConsole().sendMessage("§e[v4guard-plugin] (Bungee) Enabling...");
        new Metrics(this, 16219);
        try {
            core = new v4GuardCore(v4GuardMode.BUNGEE);
            core.getCheckManager().addProcessor(new BungeeCheckProcessor());
        } catch (Exception e) {
            this.getProxy().getConsole().sendMessage("§c[v4guard-plugin] (Bungee) Enabling... [ERROR]");
            this.getProxy().getConsole().sendMessage("§cPlease check the console for more information and report this error.");
            e.printStackTrace();
            return;
        }
        v4GuardBungee = this;
        this.getProxy().getPluginManager().registerListener(this, new AntiVPNListener());
        this.getProxy().getConsole().sendMessage("§e[v4guard-plugin] (Bungee) Enabling... [DONE]");
        this.messager = new Messager();
    }

    public Messager getMessager() {
        return messager;
    }

    public static v4GuardBungee getV4Guard() {
        return v4GuardBungee;
    }

    public static v4GuardCore getCoreInstance() {
        return core;
    }
}
