package io.v4guard.connector.platform.bungee.task;

import com.github.benmanes.caffeine.cache.Cache;
import io.v4guard.connector.common.compatibility.kick.AwaitingKick;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.Protocol;

public class AwaitingKickTask implements Runnable {

    private final Cache<String, AwaitingKick<String>> awaitedKickTaskCache;
    private final ProxyServer proxyServer;

    public AwaitingKickTask(Cache<String, AwaitingKick<String>> awaitedKickTaskCache, ProxyServer proxyServer) {
        this.awaitedKickTaskCache = awaitedKickTaskCache;
        this.proxyServer = proxyServer;
    }

    @Override
    public void run() {
        awaitedKickTaskCache.asMap().forEach((playerName, kick) -> {
            ProxiedPlayer player = proxyServer.getPlayer(playerName);
            if (player == null) return;

            if (!(player instanceof UserConnection userConnection)) return;
            if (userConnection.getCh().getEncodeProtocol() != Protocol.GAME) return;

            player.disconnect(TextComponent.fromLegacy(kick.getReason()));
            awaitedKickTaskCache.invalidate(playerName);
        });
    }
}
