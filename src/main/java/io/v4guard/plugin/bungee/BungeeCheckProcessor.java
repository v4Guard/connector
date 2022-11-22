package io.v4guard.plugin.bungee;

import com.google.common.net.InternetDomainName;
import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.check.common.CheckStatus;
import io.v4guard.plugin.core.check.common.VPNCheck;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.StringUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import org.bson.Document;

import java.util.List;

public class BungeeCheckProcessor implements CheckProcessor {

    @Override
    public void onPreLogin(String username, Object event) {
        PreLoginEvent e = (PreLoginEvent) event;

        //Clear other checks if player changes his address
        String address = e.getConnection().getSocketAddress().toString();
        v4GuardBungee.getCoreInstance().getCheckManager().cleanupChecks(username);

        if(v4GuardBungee.getCoreInstance().getBackendConnector() == null|| v4GuardBungee.getCoreInstance().getBackendConnector().getSettings() == null) return;
        final boolean wait = (boolean) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("waitResponse", false);
        if (wait) {
            e.registerIntent(v4GuardBungee.getV4Guard());
        }
        new CompletableNameCheckTask(e.getConnection().getName()) {
            @Override
            public void complete(boolean nameIsValid) {
                if(nameIsValid){

                    Document privacySettings = (Document) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
                    boolean anonVirtualHost = privacySettings.getBoolean("anonVirtualHost", true);

                    String virtualHost = e.getConnection().getVirtualHost().getHostString();
                    String mainHost = virtualHost;
                    try {
                        mainHost = InternetDomainName.from(virtualHost).topPrivateDomain().toString();
                    } catch (Exception ex) { /**fails if is not a domain**/ }
                    if(anonVirtualHost && !mainHost.equals(virtualHost)){
                        virtualHost = "***." + InternetDomainName.from(virtualHost).topPrivateDomain();
                    }

                    String playerName = e.getConnection().getName();
                    int version = e.getConnection().getVersion();

                    new CompletableIPCheckTask(address, playerName, version, virtualHost){
                        @Override
                        public void complete() {
                            VPNCheck check = this.getCheck();
                            if (wait) {
                                if (check.getStatus() == CheckStatus.USER_DENIED) {
                                    e.setCancelled(true);
                                    e.setCancelReason(TextComponent.fromLegacyText(check.getReason()));
                                }
                                e.completeIntent(v4GuardBungee.getV4Guard());
                            } else {
                                if (check.getStatus() == CheckStatus.USER_DENIED) {
                                    //Try kick player if is online
                                    onExpire(check);
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
    public void onPostLogin(String username, Object event) {
        VPNCheck check = v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatus(username);
        if(check == null) return;
        check.setPostLoginTime(System.currentTimeMillis());
        if(check.getStatus() == CheckStatus.USER_DENIED){
            PostLoginEvent e = (PostLoginEvent) event;
            ProxiedPlayer player = e.getPlayer();
            player.disconnect(TextComponent.fromLegacyText(check.getReason()));
        }
    }

    @Override
    public boolean onExpire(VPNCheck check) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(check.getName());
        if (check.getStatus() == CheckStatus.USER_DENIED && player != null) {
            player.disconnect(TextComponent.fromLegacyText(check.getReason()));
            return true;
        }
        return false;
    }

    @Override
    public void kickPlayer(String username, String reason){
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(username);
        if (player != null) {
            player.disconnect(TextComponent.fromLegacyText(reason));
        }
    }

    @Override
    public boolean isPlayerOnline(String username) {
        return ProxyServer.getInstance().getPlayer(username) != null;
    }

    @Override
    public String getPlayerServer(String username) {
        if(!isPlayerOnline(username)) return null;
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(username);
        if (player != null) {
            return player.getServer().getInfo().getName();
        }
        return null;
    }
}
