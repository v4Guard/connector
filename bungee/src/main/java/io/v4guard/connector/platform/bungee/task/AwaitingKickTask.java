package io.v4guard.connector.platform.bungee.task;

import com.github.benmanes.caffeine.cache.Cache;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.Protocol;

public class AwaitingKickTask implements Runnable {

    private final Cache<String, AwaitingKick<ProxiedPlayer>> awaitedKickTaskCache;

    public AwaitingKickTask(Cache<String, AwaitingKick<ProxiedPlayer>> awaitedKickTaskCache) {
        this.awaitedKickTaskCache = awaitedKickTaskCache;
    }

    @Override
    public void run() {
        awaitedKickTaskCache.asMap().forEach((playerName, kick) -> {
            ProxiedPlayer player = kick.getPlayer();

            if (!(player instanceof UserConnection userConnection)) {
                return;
            }

            if (userConnection.getCh().getEncodeProtocol() != Protocol.GAME) return;

            player.disconnect(TextComponent.fromLegacy(kick.getReason()));
            awaitedKickTaskCache.invalidate(playerName);
        });
    }
}
