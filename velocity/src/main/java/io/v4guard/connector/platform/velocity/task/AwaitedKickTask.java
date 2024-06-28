package io.v4guard.connector.platform.velocity.task;

import com.github.benmanes.caffeine.cache.Cache;
import com.velocitypowered.api.network.ProtocolState;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class AwaitedKickTask implements Runnable {

    private final Cache<Player, String> awaitedKickTaskCache;

    public AwaitedKickTask(Cache<Player, String> awaitedKickTaskCache) {
        this.awaitedKickTaskCache = awaitedKickTaskCache;
    }

    @Override
    public void run() {
        awaitedKickTaskCache.asMap().forEach((player, reason) -> {
            if (!player.isActive()) {
                awaitedKickTaskCache.invalidate(player);
                return;
            }

            if (player.getProtocolState() != ProtocolState.PLAY) return;

            player.disconnect(Component.text(reason));
            awaitedKickTaskCache.invalidate(player);
        });
    }
}
