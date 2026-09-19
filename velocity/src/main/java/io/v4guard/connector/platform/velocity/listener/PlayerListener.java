package io.v4guard.connector.platform.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent.LoginStatus;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.FakeChannelDetector;
import io.v4guard.connector.platform.velocity.VelocityInstance;

import java.util.logging.Level;

public class PlayerListener {

    private final VelocityInstance plugin;
    private final CoreInstance coreInstance;

    public PlayerListener(VelocityInstance plugin) {
        this.plugin = plugin;
        this.coreInstance = CoreInstance.get();
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncLogin(LoginEvent event, Continuation continuation) {
        if (!event.getResult().isAllowed() || !coreInstance.getRemoteConnection().isReady())  {
            continuation.resume();
            return;
        }

        Player player = event.getPlayer();

        try {
            ConnectedPlayer connectedPlayer = (ConnectedPlayer) player;
            if (FakeChannelDetector.isFakeChannel(connectedPlayer)) {
                // If the player is a fake player, we don't want to process the event. As we cannot kick the user
                return;
            }
        } catch (ClassCastException e) {
            UnifiedLogger.get().log(Level.WARNING, "We were unable to check if the user is a fake player, are you using a forked version of Velocity?", e);
        }

        plugin.getCheckProcessor().onEvent(player.getUsername(), event, continuation);
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onProxyDisconnect(DisconnectEvent event, Continuation continuation) {
        if (event.getLoginStatus() == LoginStatus.CONFLICTING_LOGIN) {
            continuation.resume();
            return;
        }

        coreInstance.getCheckDataCache().cleanup(event.getPlayer().getUsername());
        continuation.resume();
    }

}
