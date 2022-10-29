package io.v4guard.plugin.bungee;

import io.v4guard.plugin.bungee.accounts.BungeeMessageReceiver;
import io.v4guard.plugin.bungee.listener.AntiVPNListener;
import io.v4guard.plugin.bungee.messager.Messager;
import io.v4guard.plugin.core.accounts.AccountShieldManager;
import io.v4guard.plugin.core.mode.v4GuardMode;
import io.v4guard.plugin.core.v4GuardCore;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

public class v4GuardBungee extends Plugin {

    private static v4GuardCore core;
    private static v4GuardBungee v4GuardBungee;
    private Messager messager;

    @Override
    public void onEnable() {
        this.getProxy().getConsole().sendMessage(
                new ComponentBuilder("[v4guard-plugin] (Bungee) Enabling...")
                        .color(ChatColor.YELLOW).create()
        );

        this.getProxy().getConsole().sendMessage(new ComponentBuilder("[v4guard-plugin] (Bungee) Remember to allow Metrics on your firewall")
                .color(ChatColor.YELLOW).create()
        );

        new Metrics(this, 16219);

        try {
            core = new v4GuardCore(v4GuardMode.BUNGEE);
            core.getCheckManager().addProcessor(new BungeeCheckProcessor());
            core.setAccountShieldManager(new AccountShieldManager(new BungeeMessageReceiver(this)));
        } catch (Exception e) {
            this.getProxy().getConsole().sendMessage(
                    new ComponentBuilder("[v4guard-plugin] (Bungee) Enabling... [ERROR]")
                            .color(ChatColor.RED).create()
            );
            this.getProxy().getConsole().sendMessage(
                    new ComponentBuilder("Please check the console for more information and report this error.")
                            .color(ChatColor.RED).create()
            );
            e.printStackTrace();
            return;
        }
        v4GuardBungee = this;
        this.getProxy().getPluginManager().registerListener(this, new AntiVPNListener());
        this.getProxy().getConsole().sendMessage(
                new ComponentBuilder("[v4guard-plugin] (Bungee) Enabling... [DONE]")
                        .color(ChatColor.RED).create()
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
