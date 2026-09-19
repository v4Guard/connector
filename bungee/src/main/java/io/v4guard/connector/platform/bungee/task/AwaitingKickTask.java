package io.v4guard.connector.platform.bungee.task;

import com.github.benmanes.caffeine.cache.Cache;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.ComponentAdapter;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.Protocol;

import java.util.logging.Level;

public class AwaitingKickTask implements Runnable {

    private final ComponentAdapter<BaseComponent[]> componentAdapter;
    private final Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private final ProxyServer proxyServer;

    public AwaitingKickTask(
            ComponentAdapter<BaseComponent[]> componentAdapter,
            Cache<String, AwaitingKick<String>> awaitedKickTaskCache,
            ProxyServer proxyServer
    ) {
        this.componentAdapter = componentAdapter;
        this.awaitedKickTaskCache = awaitedKickTaskCache;
        this.proxyServer = proxyServer;
    }

    @Override
    public void run() {
        awaitedKickTaskCache.asMap().forEach((playerName, kick) -> {
            ProxiedPlayer player = proxyServer.getPlayer(playerName);
            if (player == null) return;

            if (!player.getName().equals(playerName)) {
                UnifiedLogger.get().log(Level.WARNING,
                        "Player " + playerName + " is not equal to the player's name "
                                + player.getName() + " removing await kick task. And notify us, " +
                                "this is UEB from the backend!"
                );
                awaitedKickTaskCache.invalidate(playerName);
                return;
            }

            if (!(player instanceof UserConnection userConnection)
                    || userConnection.getCh().getEncodeProtocol() != Protocol.GAME) return;

            player.disconnect(componentAdapter.adapt(kick.getReason(), userConnection.getCh().getEncodeVersion() < 735));
            awaitedKickTaskCache.invalidate(playerName);
        });
    }
}
