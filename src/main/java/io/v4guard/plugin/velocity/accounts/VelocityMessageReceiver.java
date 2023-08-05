package io.v4guard.plugin.velocity.accounts;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.accounts.MessageReceiver;
import io.v4guard.plugin.core.accounts.auth.AuthType;
import io.v4guard.plugin.core.accounts.auth.Authentication;
import io.v4guard.plugin.core.constants.ShieldChannels;

public class VelocityMessageReceiver extends MessageReceiver {

    @Subscribe(order = PostOrder.FIRST)
    public void onMessage(PluginMessageEvent event) {
        if (!ShieldChannels.VELOCITY_CHANNEL.equals(event.getIdentifier().getId())) {
            return;
        }

        //event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        processPluginMessage(event.getData());

    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event) {
        if (CoreInstance.get().isAccountShieldFound()) {
            return;
        }

        Player player = event.getPlayer();

        if (player.isOnlineMode()) {
            Authentication auth = new Authentication(
                    player.getUsername()
                    , player.getUniqueId()
                    , AuthType.MOJANG
                    , player.hasPermission("v4guard.accshield")
            );

            CoreInstance.get().getAccountShieldSender().sendSocketMessage(auth);
        }
    }

}
