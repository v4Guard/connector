package io.v4guard.connector.platform.bungee.task;

import com.github.benmanes.caffeine.cache.Cache;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.Protocol;

import java.util.UUID;

public class AwaitingKickTask implements Runnable {

    private final Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private final ProxyServer server;

    public AwaitingKickTask(Cache<String, AwaitingKick<String>> awaitedKickTaskCache, ProxyServer server) {
        this.server = server;
        this.awaitedKickTaskCache = awaitedKickTaskCache;
    }

    @Override
    public void run() {
        awaitedKickTaskCache.asMap().forEach((playerName, kick) -> {
            ProxiedPlayer player = server.getPlayer(playerName);
            if (player == null) {
                //user might be not be processed yet by the proxy or the player might have been disconnected we ignore it
                //for now the cache should remove when the proxy timeout ocurrs
                return;
            }

            if (!(player instanceof UserConnection userConnection)) {
                //User has an invalid state for whatever reason
                awaitedKickTaskCache.invalidate(playerName);
                return;
            }

            if (userConnection.getCh().getEncodeProtocol() != Protocol.GAME) return;

            player.disconnect(TextComponent.fromLegacy(kick.getReason()));
            awaitedKickTaskCache.invalidate(playerName);
        });
    }
}
