package io.v4guard.connector.platform.velocity.task;

import com.github.benmanes.caffeine.cache.Cache;
import com.velocitypowered.api.network.ProtocolState;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.ComponentAdapter;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import net.kyori.adventure.text.Component;

import java.util.logging.Level;

public class AwaitingKickTask implements Runnable {

    private final ComponentAdapter<Component> componentAdapter;
    private final Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private final ProxyServer server;

    public AwaitingKickTask(
            Cache<String, AwaitingKick<String>> awaitedKickTaskCache,
            ProxyServer server,
            ComponentAdapter<Component> componentAdapter
    ) {
        this.awaitedKickTaskCache = awaitedKickTaskCache;
        this.server = server;
        this.componentAdapter = componentAdapter;
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

            Component component = componentAdapter.adapt(
                    kick.getReason(),
                    player.getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_16.getProtocol()
            );
            if (component == null)
                component = Component.text("An error occurred while processing your login");

            player.disconnect(component);
            awaitedKickTaskCache.invalidate(playerName);
        });
    }
}
