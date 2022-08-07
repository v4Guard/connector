package io.v4guard.plugin.bungee.listener;

import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.StringUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.bson.Document;

import java.util.List;

public class AntiVPNListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent e) {
        BackendConnector conn = v4GuardBungee.getCoreInstance().getBackendConnector();
        if (!conn.getSocketStatus().equals(SocketStatus.AUTHENTICATED)) {
            return;
        }
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
                            if (this.isBlocked()) {
                                String username = this.getUsername();
                                String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("kick"));
                                Document data = (Document) this.getData().get("result");
                                kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, (Document) data.get("variables"));
                                ProxiedPlayer pp = ProxyServer.getInstance().getPlayer(username);
                                if(pp != null) {
                                    pp.disconnect(TextComponent.fromLegacyText(kickReasonMessage));
                                } else {
                                    e.setCancelled(true);
                                    e.setCancelReason(TextComponent.fromLegacyText(kickReasonMessage));
                                }
                            }
                            if (wait) {
                                e.completeIntent(v4GuardBungee.getV4Guard());
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
}
