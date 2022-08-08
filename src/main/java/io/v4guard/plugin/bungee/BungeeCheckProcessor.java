package io.v4guard.plugin.bungee;

import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.CheckStatus;
import io.v4guard.plugin.core.utils.StringUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import org.bson.Document;

import java.util.List;

public class BungeeCheckProcessor implements CheckProcessor {

    @Override
    public void onPreLogin(String username, Object event) {
        PreLoginEvent e = (PreLoginEvent) event; BackendConnector conn = v4GuardBungee.getCoreInstance().getBackendConnector();
        if (!conn.getSocketStatus().equals(SocketStatus.AUTHENTICATED)) {
            return;
        }
        v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatusMap().remove(e.getConnection().getName());
        Document kickMessages = (Document) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().get("messages");
        final boolean wait = (boolean) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        if (wait) {
            e.registerIntent(v4GuardBungee.getV4Guard());
        }
        new CompletableNameCheckTask(e.getConnection().getName()) {
            @Override
            public void complete(boolean nameIsValid) {
                if(nameIsValid){
                    new CompletableIPCheckTask(e.getConnection().getAddress().getAddress().getHostAddress(), e.getConnection().getName(), e.getConnection().getVersion()){
                        @Override
                        public void complete() {
                            String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("kick"));
                            Document data = (Document) this.getData().get("result");
                            kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, (Document) data.get("variables"));
                            String username = this.getUsername();
                            if (wait) {
                                if (this.isBlocked()) {
                                    e.setCancelled(true);
                                    e.setCancelReason(TextComponent.fromLegacyText(kickReasonMessage));
                                    e.completeIntent(v4GuardBungee.getV4Guard());
                                }
                            } else {
                                if (this.isBlocked()) {
                                    v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatusMap().put(username, new CheckStatus(username, kickReasonMessage, true));
                                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(username);
                                    if (player != null) {
                                        player.disconnect(TextComponent.fromLegacyText(kickReasonMessage));
                                    }
                                }
                            }
                        }
                    };
                } else {
                    String username = this.getUsername();
                    String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("invalidUsername"));
                    kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, new Document("username", username));
                    e.setCancelled(true);
                    e.setCancelReason(TextComponent.fromLegacyText(kickReasonMessage));
                }
            }
        };
    }

    @Override
    public void onLogin(String username, Object event) {
        LoginEvent e = (LoginEvent) event;
        CheckStatus status = v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatus(e.getConnection().getName());
        if(status != null && status.isBlocked()){
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText(status.getReason()));
            v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatusMap().remove(e.getConnection().getName());
        }
    }

    @Override
    public void onPostLogin(String username, Object event) {
        PostLoginEvent e = (PostLoginEvent) event;
        ProxiedPlayer player = e.getPlayer();
        if(player == null) return;
        CheckStatus status = v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatus(player.getName());
        if(status != null && status.isBlocked()){
            player.disconnect(TextComponent.fromLegacyText(status.getReason()));
            v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatusMap().remove(player.getName());
        }
    }
}
