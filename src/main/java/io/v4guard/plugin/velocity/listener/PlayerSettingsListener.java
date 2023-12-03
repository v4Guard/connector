package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerSettingsChangedEvent;
import com.velocitypowered.api.proxy.player.PlayerSettings;
import com.velocitypowered.api.proxy.player.SkinParts;
import io.v4guard.plugin.core.check.settings.PlayerSettingsCheckProcessor;

public class PlayerSettingsListener extends PlayerSettingsCheckProcessor {

    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerPostLogin(PlayerSettingsChangedEvent event) {
        PlayerSettings mcSettings  = event.getPlayer().getPlayerSettings();
        SkinParts skinParts = mcSettings.getSkinParts();

        super.process(
                event.getPlayer().getUsername(), event.getPlayer().getUniqueId(),
                mcSettings.getLocale() != null ? mcSettings.getLocale().toLanguageTag() : "Unknown",
                String.valueOf(mcSettings.getViewDistance()), String.valueOf(mcSettings.hasChatColors()),
                mcSettings.getMainHand().name(), mcSettings.getChatMode().name(), String.valueOf(mcSettings.isClientListingAllowed()),
                String.valueOf(skinParts.hasHat()), String.valueOf(skinParts.hasCape()), String.valueOf(skinParts.hasJacket()),
                String.valueOf(skinParts.hasLeftSleeve()), String.valueOf(skinParts.hasRightSleeve()),
                String.valueOf(skinParts.hasLeftPants()), String.valueOf(skinParts.hasLeftPants())
        );
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        super.onPlayerDisconnect(event.getPlayer().getUniqueId());
    }
}
