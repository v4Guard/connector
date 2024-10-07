package io.v4guard.connector.platform.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerSettingsChangedEvent;
import com.velocitypowered.api.proxy.player.PlayerSettings;
import com.velocitypowered.api.proxy.player.SkinParts;
import io.v4guard.connector.common.check.settings.MinecraftSettings;
import io.v4guard.connector.common.check.settings.PlayerSettingsCheckProcessor;

public class PlayerSettingsListener extends PlayerSettingsCheckProcessor {

    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerSettingsChanged(PlayerSettingsChangedEvent event) {
        PlayerSettings settings  = event.getPlayer().getPlayerSettings();
        SkinParts skinParts = settings.getSkinParts();

        MinecraftSettings allSettings = MinecraftSettings.builder()
                .locale(settings.getLocale() != null ? settings.getLocale().toLanguageTag() : "Unknown")
                .viewDistance(String.valueOf(settings.getViewDistance()))
                .hasColors(String.valueOf(settings.hasChatColors()))
                .mainHand(settings.getMainHand().name())
                .chatMode(settings.getChatMode().name())
                .clientListing(String.valueOf(settings.isClientListingAllowed()))
                .hasHat(String.valueOf(skinParts.hasHat()))
                .hasCape(String.valueOf(skinParts.hasCape()))
                .hasJacket(String.valueOf(skinParts.hasJacket()))
                .hasLeftSleeve(String.valueOf(skinParts.hasLeftSleeve()))
                .hasRightSleeve(String.valueOf(skinParts.hasRightSleeve()))
                .hasLeftPants(String.valueOf(skinParts.hasLeftPants()))
                .hasRightPants(String.valueOf(skinParts.hasRightPants()))
                .build();

        super.process(event.getPlayer().getUsername(), event.getPlayer().getUniqueId(), allSettings);
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        super.onPlayerDisconnect(event.getPlayer().getUniqueId());
    }
}
