package io.v4guard.connector.platform.bungee.listener;

import io.v4guard.connector.common.check.settings.MinecraftSettings;
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
    public void onSettingsChanged(SettingsChangedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        SkinConfiguration skinParts = event.getPlayer().getSkinParts();

        MinecraftSettings allSettings = MinecraftSettings.builder()
                .locale(player.getLocale() != null ? player.getLocale().toLanguageTag() : "Unknown")
                .viewDistance(String.valueOf(player.getViewDistance()))
                .hasColors(String.valueOf(player.hasChatColors()))
                .mainHand(player.getMainHand().name())
                .chatMode(player.getChatMode().name())
                .clientListing(null) // Cannot be obtained via BungeeCord API
                .hasHat(String.valueOf(skinParts.hasHat()))
                .hasCape(String.valueOf(skinParts.hasCape()))
                .hasJacket(String.valueOf(skinParts.hasJacket()))
                .hasLeftSleeve(String.valueOf(skinParts.hasLeftSleeve()))
                .hasRightSleeve(String.valueOf(skinParts.hasRightSleeve()))
                .hasLeftPants(String.valueOf(skinParts.hasLeftPants()))
                .hasRightPants(String.valueOf(skinParts.hasRightPants()))
                .build();

        super.process(event.getPlayer().getName(), event.getPlayer().getUniqueId(), allSettings);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        super.onPlayerDisconnect(event.getPlayer().getUniqueId());
    }
}
