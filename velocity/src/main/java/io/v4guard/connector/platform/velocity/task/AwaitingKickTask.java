package io.v4guard.connector.platform.velocity.task;

import com.github.benmanes.caffeine.cache.Cache;
import com.velocitypowered.api.network.ProtocolState;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import net.kyori.adventure.text.Component;

import java.util.logging.Level;

public class AwaitingKickTask implements Runnable {

    private final Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private final ProxyServer server;

    public AwaitingKickTask(Cache<String, AwaitingKick<String>> awaitedKickTaskCache, ProxyServer server) {
        this.awaitedKickTaskCache = awaitedKickTaskCache;
        this.server = server;
    }

    @Override
    public void run() {
        awaitedKickTaskCache.asMap().forEach((playerName, kick) -> {
            Player player = server.getPlayer(playerName).orElse(null);
            if (player == null) {
                return;
            }

            if (!player.getUsername().equals(playerName)) {
                UnifiedLogger.get().log(Level.WARNING,
                        "Player " + playerName + " is not equal to the player's name "
                                + player.getUsername() + " removing await kick task. And notify us, " +
                                "this is UEB from the backend!"
                );
                awaitedKickTaskCache.invalidate(playerName);
                return;
            }

            if (!player.isActive() || player.getProtocolState() != ProtocolState.PLAY) {
                return;
            }
            
            player.disconnect(Component.text(kick.getReason()));
            awaitedKickTaskCache.invalidate(playerName);
        });
    }
}
