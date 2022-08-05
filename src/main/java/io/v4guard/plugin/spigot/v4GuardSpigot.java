package io.v4guard.plugin.spigot;

import io.v4guard.plugin.bungee.messager.Messager;
import io.v4guard.plugin.core.mode.v4GuardMode;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.spigot.listener.AntiVPNListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class v4GuardSpigot extends JavaPlugin {

    private static v4GuardCore core;
    private static v4GuardSpigot v4GuardSpigot;
    private Messager messager;

    @Override
    public void onEnable(){
        this.getServer().getConsoleSender().sendMessage("§e[v4guard-plugin] (Spigot) Enabling...");
        try {
            core = new v4GuardCore();
        } catch (Exception e) {
            this.getServer().getConsoleSender().sendMessage("§c[v4guard-plugin] (Spigot) Enabling... [ERROR]");
            this.getServer().getConsoleSender().sendMessage("§cPlease check the console for more information and report this error.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
        core.setPluginMode(v4GuardMode.SPIGOT);
        v4GuardSpigot = this;
        this.getServer().getPluginManager().registerEvents(new AntiVPNListener(), this);
        this.getServer().getConsoleSender().sendMessage("§e[v4guard-plugin] (Spigot) Enabling... [DONE]");
        this.messager = new Messager();
    }

    public Messager getMessager() {
        return messager;
    }

    public static v4GuardCore getCoreInstance() {
        return core;
    }

    public static v4GuardSpigot getV4Guard() {
        return v4GuardSpigot;
    }
}
