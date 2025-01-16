package io.v4guard.connector.platform.bungee.listener;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.accounts.auth.AuthType;
import io.v4guard.connector.common.accounts.auth.Authentication;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.platform.bungee.BungeeInstance;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerListener implements Listener {

    private final BungeeInstance plugin;
    private final CoreInstance coreInstance;

    public PlayerListener(BungeeInstance plugin, CoreInstance coreInstance) {
        this.plugin = plugin;
        this.coreInstance = coreInstance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent event) {

        PendingConnection connection = event.getConnection();

        if (event.isCancelled() || connection == null || !coreInstance.getRemoteConnection().isReady()) {
            return;
        }

        plugin.getCheckProcessor().onEvent(event.getConnection().getName(), event);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLoginEvent(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        String playerName = event.getPlayer().getName();
        PlayerCheckData checkData = plugin.getCheckDataCache().getTempCheckData(playerName);

        if (checkData != null) {
            plugin.getCheckDataCache().cache(playerName, checkData);
        }

        if (coreInstance.isAccountShieldFound()) {
            return;
        }

        if (player.getPendingConnection().isOnlineMode()) {
            Authentication auth = new Authentication(
                    player.getName(),
                    player.getUniqueId(),
                    AuthType.MOJANG,
                    player.hasPermission("v4guard.accshield")
            );

            coreInstance.getAccountShieldSender().sendSocketMessage(auth);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProxyDisconnect(PlayerDisconnectEvent event) {
        coreInstance.getCheckDataCache().cleanup(event.getPlayer().getName());
    }

}
