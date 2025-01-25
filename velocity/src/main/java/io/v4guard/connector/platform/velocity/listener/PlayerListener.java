package io.v4guard.connector.platform.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent.LoginStatus;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.accounts.auth.AuthType;
import io.v4guard.connector.common.accounts.auth.Authentication;
import io.v4guard.connector.platform.velocity.VelocityInstance;

public class PlayerListener {

    private final VelocityInstance plugin;
    private final CoreInstance coreInstance;

    public PlayerListener(VelocityInstance plugin) {
        this.plugin = plugin;
        this.coreInstance = CoreInstance.get();
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncLogin(LoginEvent event, Continuation continuation) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        if (coreInstance.getRemoteConnection().isReady()) {
            plugin.getCheckProcessor().onEvent(event.getPlayer().getUsername(), event, continuation);
        } else {
            continuation.resume();
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event, Continuation continuation) {
        if (coreInstance.isAccountShieldFound()) {
            continuation.resume();
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

            coreInstance.getAccountShieldSender().sendSocketMessage(auth);
        }

        continuation.resume();
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onProxyDisconnect(DisconnectEvent event) {
        if (event.getLoginStatus() == LoginStatus.SUCCESSFUL_LOGIN) {
            coreInstance.getCheckDataCache().cleanup(event.getPlayer().getUsername());
        }
    }

}
