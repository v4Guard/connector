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
        CheckStatus status = v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatus(e.getConnection().getName());
        if (status != null && status.isBlocked()) {
            e.setCancelled(true);
            e.setCancelReason(TextComponent.fromLegacyText(status.getReason()));
            return;
        }
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
                            CheckStatus check = this.getCheck();
                            if (wait) {
                                if (check.isBlocked()) {
                                    e.setCancelled(true);
                                    e.setCancelReason(TextComponent.fromLegacyText(check.getReason()));
                                    e.completeIntent(v4GuardBungee.getV4Guard());
                                }
                            } else {
                                if (check.isBlocked()) {
                                    //Try kick player if is online
                                    actionOnExpire(check);
                                }
                            }
                        }
                    };
                } else {
                    String username = this.getUsername();
                    Document kickMessages = (Document) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().get("messages");
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
        }
    }

    @Override
    public void onPostLogin(String username, Object event) {
        PostLoginEvent e = (PostLoginEvent) event;
        ProxiedPlayer player = e.getPlayer();
        if(player == null) return;
        CheckStatus status = v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatus(player.getName());
        if(status != null && status.isBlocked()) player.disconnect(TextComponent.fromLegacyText(status.getReason()));
    }

    @Override
    public boolean actionOnExpire(CheckStatus check) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(check.getName());
        if (check.isBlocked() && player != null) {
            player.disconnect(TextComponent.fromLegacyText(check.getReason()));
            return true;
        }
        return false;

    }
}
