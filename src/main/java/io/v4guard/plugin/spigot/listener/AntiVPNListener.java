package io.v4guard.plugin.spigot.listener;

import io.v4guard.plugin.spigot.v4GuardSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class AntiVPNListener implements Listener {

    @EventHandler
    public void onPreLogin(final AsyncPlayerPreLoginEvent e) {
        v4GuardSpigot.getCoreInstance().getCheckManager().runPreLoginCheck(e.getName(), e);
    }

    @EventHandler
    public void onLogin(final PlayerLoginEvent e) {
        v4GuardSpigot.getCoreInstance().getCheckManager().runLoginCheck(e.getPlayer().getName(), e);
    }

    @EventHandler
    public void onPostLogin(final PlayerJoinEvent e) {
        v4GuardSpigot.getCoreInstance().getCheckManager().runPostLoginCheck(e.getPlayer().getName(), e);
    }

}
