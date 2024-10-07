package io.v4guard.connector.platform.velocity.task;

import com.github.benmanes.caffeine.cache.Cache;
import com.velocitypowered.api.network.ProtocolState;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import net.kyori.adventure.text.Component;

public class AwaitingKickTask implements Runnable {

    private final Cache<String, AwaitingKick<Player>> awaitedKickTaskCache;

    public AwaitingKickTask(Cache<String, AwaitingKick<Player>> awaitedKickTaskCache) {
        this.awaitedKickTaskCache = awaitedKickTaskCache;
    }

    @Override
    public void run() {
        awaitedKickTaskCache.asMap().forEach((playerName, kick) -> {
            Player player = kick.getPlayer();

            if (!player.isActive()) {
                awaitedKickTaskCache.invalidate(playerName);
                return;
            }

            if (player.getProtocolState() != ProtocolState.PLAY) {
                return;
            }

            player.disconnect(Component.text(kick.getReason()));
            awaitedKickTaskCache.invalidate(playerName);
        });
    }
}
