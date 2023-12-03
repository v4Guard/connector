package io.v4guard.plugin.bungee.listener;

import io.v4guard.plugin.core.check.brand.BrandCheckProcessor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessagingListener extends BrandCheckProcessor implements Listener {

    @EventHandler
    public void onChannelMessage(PluginMessageEvent e) {
        if (!(e.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) e.getSender();

        super.process(player.getName(), player.getUniqueId(), e.getTag(), e.getData());
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        super.onPlayerDisconnect(event.getPlayer().getUniqueId());
    }
}
