package io.v4guard.plugin.spigot.listener;

import io.v4guard.plugin.spigot.v4GuardSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class AntiVPNListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(final AsyncPlayerPreLoginEvent e) {
        v4GuardSpigot.getCoreInstance().getCheckManager().runPreLoginCheck(e.getName(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostLogin(PlayerJoinEvent e) {
        v4GuardSpigot.getCoreInstance().getCheckManager().runPostLoginCheck(e.getPlayer().getName(), e);
    }

}
