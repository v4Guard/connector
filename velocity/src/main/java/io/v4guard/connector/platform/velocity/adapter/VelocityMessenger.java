package io.v4guard.connector.platform.velocity.adapter;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.v4guard.connector.common.compatibility.ComponentAdapter;
import io.v4guard.connector.common.compatibility.Messenger;
import io.v4guard.connector.common.compatibility.PlayerFetchResult;
import io.v4guard.connector.api.constants.ListenersConstants;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import net.kyori.adventure.text.Component;

public class VelocityMessenger implements Messenger {

    private final ProxyServer proxyServer;
    private final ComponentAdapter<Component> componentAdapter;

    public VelocityMessenger(ProxyServer proxyServer, ComponentAdapter<Component> componentAdapter) {
        this.proxyServer = proxyServer;
        this.componentAdapter = componentAdapter;
    }

    @Override
    public void broadcastWithPermission(String message, String permission) {
        boolean sendToAll = permission.equals(ListenersConstants.ALL_PLAYERS_PERMISSION);

        Component component = this.componentAdapter.adapt(message);
        if (component == null) return;

        for (Player player : this.proxyServer.getAllPlayers()) {
            if (!sendToAll && !player.hasPermission(permission)) continue;

            player.sendMessage(component);
        }
    }

    @Override
    public void sendMessageTo(String playerName, String message) {
        PlayerFetchResult<Player> fetchedPlayer = VelocityInstance.get().fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            Component component = this.componentAdapter.adapt(message);
            if (component == null) return;
            fetchedPlayer.getPlayer().sendMessage(component);
        }
    }
}
