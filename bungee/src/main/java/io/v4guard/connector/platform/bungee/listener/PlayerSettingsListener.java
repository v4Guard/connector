package io.v4guard.connector.platform.bungee.listener;

import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;
import net.md_5.bungee.api.SkinConfiguration;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.SettingsChangedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerSettingsListener extends PlayerSettingsCheckProcessor implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSettingsChange(SettingsChangedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        SkinConfiguration mcSettings = event.getPlayer().getSkinParts();

        super.process(
                event.getPlayer().getName(), event.getPlayer().getUniqueId(),
                player.getLocale() != null ? player.getLocale().toLanguageTag() : "Unknown",
                String.valueOf(player.getViewDistance()), String.valueOf(player.hasChatColors()),
                player.getMainHand().name(), player.getChatMode().name(), null, String.valueOf(mcSettings.hasHat()),
                String.valueOf(mcSettings.hasCape()), String.valueOf(mcSettings.hasJacket()), String.valueOf(mcSettings.hasLeftSleeve()),
                String.valueOf(mcSettings.hasRightSleeve()), String.valueOf(mcSettings.hasLeftPants()), String.valueOf(mcSettings.hasLeftPants())
        );
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        super.onPlayerDisconnect(event.getPlayer().getUniqueId());
    }
}
