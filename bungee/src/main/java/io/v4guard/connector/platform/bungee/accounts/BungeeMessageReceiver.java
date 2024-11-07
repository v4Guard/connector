package io.v4guard.connector.platform.bungee.accounts;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.accounts.MessageReceiver;
import io.v4guard.connector.common.accounts.auth.AuthType;
import io.v4guard.connector.common.accounts.auth.Authentication;
import io.v4guard.connector.common.constants.ShieldChannels;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeMessageReceiver extends MessageReceiver implements Listener {

    private final CoreInstance coreInstance;

    public BungeeMessageReceiver(CoreInstance coreInstance) {
        this.coreInstance = coreInstance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMessage(PluginMessageEvent event) {
        if (!event.getTag().equals(ShieldChannels.BUNGEE_CHANNEL)) {
            return;
        }

        if (!(event.getSender() instanceof Server)) {
            return;
        }

        super.processPluginMessage(event.getData());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(PostLoginEvent e) {
        if (coreInstance.isAccountShieldFound()) {
            return;
        }

        ProxiedPlayer player = e.getPlayer();

        if (player.getPendingConnection().isOnlineMode()) {
            Authentication auth = new Authentication(
                    player.getName()
                    , player.getUniqueId()
                    , AuthType.MOJANG
                    , player.hasPermission("v4guard.accshield")
            );

            coreInstance.getAccountShieldSender().sendSocketMessage(auth);
        }
    }

}
