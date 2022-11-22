package io.v4guard.plugin.bungee;

import io.v4guard.plugin.bungee.accounts.BungeeMessageReceiver;
import io.v4guard.plugin.bungee.listener.AntiVPNListener;
import io.v4guard.plugin.bungee.messager.Messager;
import io.v4guard.plugin.core.accounts.AccountShieldManager;
import io.v4guard.plugin.core.mode.v4GuardMode;
import io.v4guard.plugin.core.v4GuardCore;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public class v4GuardBungee extends Plugin {

    private static v4GuardCore core;
    private static v4GuardBungee v4GuardBungee;
    private Messager messager;

    @Override
    public void onEnable(){
        this.getProxy().getConsole().sendMessage(
                new TextComponent("§e[v4guard-plugin] (Bungee) Enabling...")
        );
        try {
            new Metrics(this, 16219);
        }
        catch (Exception ex){
            this.getProxy().getConsole().sendMessage(
                    new TextComponent("§e[v4guard-plugin] (Bungee) Failed to connect with bStats [WARN]")
            );
        }
        try {
            core = new v4GuardCore(v4GuardMode.BUNGEE);
            core.getCheckManager().addProcessor(new BungeeCheckProcessor());
            core.setAccountShieldManager(new AccountShieldManager(new BungeeMessageReceiver(this)));
        }
        catch (Exception e) {
            this.getProxy().getConsole().sendMessage(
                    new TextComponent("§c[v4guard-plugin] (Bungee) Enabling... [ERROR]")
            );
            this.getProxy().getConsole().sendMessage(
                    new TextComponent("§cPlease check the console for more information and report this error.")
            );
            e.printStackTrace();
            return;
        }
        v4GuardBungee = this;
        this.getProxy().getPluginManager().registerListener(this, new AntiVPNListener());
        this.getProxy().getConsole().sendMessage(
                new TextComponent("§e[v4guard-plugin] (Bungee) Enabling... [DONE]")
        );
        this.messager = new Messager();
        getCoreInstance().setAccountShieldFound(
                this.getProxy().getPluginManager().getPlugin("v4guard-account-shield") != null
        );
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
