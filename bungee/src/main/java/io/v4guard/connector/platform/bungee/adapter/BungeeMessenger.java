package io.v4guard.connector.platform.bungee.adapter;

import io.v4guard.connector.common.compatibility.ComponentAdapter;
import io.v4guard.connector.common.compatibility.Messenger;
import io.v4guard.connector.common.compatibility.PlayerFetchResult;
import io.v4guard.connector.api.constants.ListenersConstants;
import io.v4guard.connector.platform.bungee.BungeeInstance;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeMessenger implements Messenger {

    private final ComponentAdapter<BaseComponent[]> componentAdapter;

    public BungeeMessenger(ComponentAdapter<BaseComponent[]> componentAdapter) {
        this.componentAdapter = componentAdapter;
    }

    @Override
    public void broadcastWithPermission(String message, String permission) {
        boolean sendToAll = permission.equals(ListenersConstants.ALL_PLAYERS_PERMISSION);

        ProxiedPlayer[] players = ProxyServer.getInstance().getPlayers().toArray(new ProxiedPlayer[0]);

        for (ProxiedPlayer player : players) {
            if (sendToAll || player.hasPermission(permission)) {
                player.sendMessage(this.componentAdapter.adapt(
                        message,
                        ((UserConnection) player).getCh().getEncodeVersion() < 735
                ));
            }
        }
    }

    @Override
    public void sendMessageTo(String playerName, String message) {
        PlayerFetchResult<ProxiedPlayer> fetchedPlayer = BungeeInstance.get().fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            fetchedPlayer.getPlayer()
                    .sendMessage(this.componentAdapter.adapt(
                            message,
                            ((UserConnection) fetchedPlayer.getPlayer()).getCh().getEncodeVersion() < 735
                    ));
        }
    }
}
