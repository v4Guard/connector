package io.v4guard.connector.platform.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent.LoginStatus;
import com.velocitypowered.api.event.connection.LoginEvent;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.platform.velocity.VelocityInstance;

public class PlayerListener {

    private final VelocityInstance plugin;

    public PlayerListener(VelocityInstance plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncLogin(LoginEvent event, Continuation continuation) {
        if(!event.getResult().isAllowed()) {
            return;
        }

        if (CoreInstance.get().getRemoteConnection().isReady()) {
            plugin.getCheckProcessor().onEvent(event.getPlayer().getUsername(), event, continuation);
        } else {
            continuation.resume();
        }
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onProxyDisconnect(DisconnectEvent event) {
        if (event.getLoginStatus() == LoginStatus.SUCCESSFUL_LOGIN) {
            CoreInstance.get().getCheckDataCache().cleanup(event.getPlayer().getUsername());
        }
    }

}
