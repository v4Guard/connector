package io.v4guard.plugin.bungee.listener;

import io.v4guard.plugin.bungee.v4GuardBungee;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class AntiVPNListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent e) {
       v4GuardBungee.getCoreInstance().getCheckManager().runPreLoginCheck(e.getConnection().getName(), e);
    }

    @EventHandler
    public void onLogin(LoginEvent e){
        v4GuardBungee.getCoreInstance().getCheckManager().runLoginCheck(e.getConnection().getName(), e);
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e){
        v4GuardBungee.getCoreInstance().getCheckManager().runPostLoginCheck(e.getPlayer().getName(), e);
    }

}
