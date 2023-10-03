package io.v4guard.plugin.bungee.listener;

import io.v4guard.plugin.bungee.BungeeInstance;
import io.v4guard.plugin.bungee.cache.BungeeCheckDataCache;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.PlayerCheckData;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerListener implements Listener {

    private BungeeInstance plugin;

    public PlayerListener(BungeeInstance plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent event) {
        if (event.isCancelled()) {
            return;
        }

        PendingConnection connection = event.getConnection();

        if (connection == null) {
            return;
        }

        if (!CoreInstance.get().getBackend().isReady()) {
            return;
        }

        plugin.getCheckProcessor().onEvent(event.getConnection().getName(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLoginEvent(PostLoginEvent event) {
        String playerName = event.getPlayer().getName();
        PlayerCheckData checkData = plugin.getCheckDataCache().getTempCheckData(playerName);

        if (checkData != null) {
            plugin.getCheckDataCache().cache(playerName, checkData);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProxyDisconnect(PlayerDisconnectEvent event) {
        CoreInstance.get().getCheckDataCache().cleanup(event.getPlayer().getName());
    }

}
