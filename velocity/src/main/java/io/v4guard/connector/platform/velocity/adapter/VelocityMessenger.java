package io.v4guard.connector.platform.velocity.adapter;

import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.compatibility.Messenger;
import io.v4guard.connector.common.compatibility.PlayerFetchResult;
import io.v4guard.connector.api.constants.ListenersConstants;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import net.kyori.adventure.text.Component;

public class VelocityMessenger implements Messenger {

    private final VelocityInstance velocityInstance;

    public VelocityMessenger(VelocityInstance velocityInstance) {
        this.velocityInstance = velocityInstance;
    }

    @Override
    public void broadcastWithPermission(String message, String permission) {
        boolean sendToAll = permission.equals(ListenersConstants.ALL_PLAYERS_PERMISSION);

        Component component = velocityInstance.getLegacyComponentSerializer().deserialize(message);

        for(Player player : VelocityInstance.get().getServer().getAllPlayers()) {
            if(!sendToAll && !player.hasPermission(permission)) {
                continue;
            }

            player.sendMessage(component);
        }
    }

    @Override
    public void sendMessageTo(String playerName, String message) {
        PlayerFetchResult<Player> fetchedPlayer = VelocityInstance.get().fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            fetchedPlayer.getPlayer().sendMessage(velocityInstance.getLegacyComponentSerializer().deserialize(message));
        }
    }
}
