package io.v4guard.plugin.velocity.adapter;

import com.velocitypowered.api.proxy.Player;
import io.v4guard.plugin.core.compatibility.Messenger;
import io.v4guard.plugin.core.compatibility.PlayerFetchResult;
import io.v4guard.plugin.core.constants.ListenersConstants;
import io.v4guard.plugin.velocity.VelocityInstance;
import net.kyori.adventure.text.Component;

public class VelocityMessenger implements Messenger {
    @Override
    public void broadcastWithPermission(String message, String permission) {
        boolean sendToAll = permission.equals(ListenersConstants.ALL_PLAYERS_PERMISSION);

        for(Player player : VelocityInstance.get().getServer().getAllPlayers()) {
            if(!sendToAll && !player.hasPermission(permission)) {
                continue;
            }

            player.sendMessage(Component.text(message));
        }
    }

    @Override
    public void sendMessageTo(String playerName, String message) {
        PlayerFetchResult<Player> fetchedPlayer = VelocityInstance.get().fetchPlayer(playerName);

        if (fetchedPlayer.isOnline()) {
            fetchedPlayer.getPlayer().sendMessage(Component.text(message));
        }
    }
}
