package io.v4guard.connector.platform.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.PlayerClientBrandEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.brand.BrandCheckProcessor;

import java.nio.charset.StandardCharsets;

public class PluginMessagingListener extends BrandCheckProcessor {

    private final MinecraftChannelIdentifier MINECRAFT_BRAND_CHANNEL = MinecraftChannelIdentifier.create("minecraft", "brand");

    @Subscribe
    public void onPlayerClientBrand(PlayerClientBrandEvent event, Continuation continuation) {
        Player player = event.getPlayer();

        if (!CoreInstance.get().getRemoteConnection().isReady()) {
            continuation.resume();
        }

        super.process(
                player.getUsername()
                , player.getUniqueId()
                , MINECRAFT_BRAND_CHANNEL.getId()
                , event.getBrand().getBytes(StandardCharsets.UTF_8)
        );

        continuation.resume();
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event, Continuation continuation) {
        if (!(event.getSource() instanceof Player)) {
            continuation.resume();
            return;
        }

        if (!CoreInstance.get().getRemoteConnection().isReady()) {
            continuation.resume();
            return;
        }

        Player player = (Player) event.getSource();

        super.process(
                player.getUsername()
                , player.getUniqueId()
                , event.getIdentifier().getId()
                , event.getData()
        );

        continuation.resume();
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        super.onPlayerDisconnect(event.getPlayer().getUniqueId());
    }
}
