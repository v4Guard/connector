package io.v4guard.plugin.velocity.accounts;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.accounts.auth.AuthType;
import io.v4guard.plugin.core.accounts.auth.Authentication;
import io.v4guard.plugin.core.accounts.messaging.MessageReceiver;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import org.bson.Document;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class VelocityMessageReceiver extends MessageReceiver {

    public VelocityMessageReceiver(v4GuardVelocity plugin) {
        plugin.getServer().getEventManager().register(plugin, this);
        plugin.getServer().getChannelRegistrar().register(new LegacyChannelIdentifier(MessageReceiver.VELOCITY_CHANNEL));
        plugin.getServer().getChannelRegistrar().register(MinecraftChannelIdentifier.from(MessageReceiver.VELOCITY_CHANNEL));
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onMessage(PluginMessageEvent e){
        boolean invalidatedCache = (boolean) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);
        if(invalidatedCache) return;
        if (!e.getIdentifier().getId().equals(MessageReceiver.VELOCITY_CHANNEL)) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        try {
            String data = in.readUTF();
            Document doc = Document.parse(data);
            Authentication auth = Authentication.deserialize(doc);
            v4GuardCore.getInstance().getAccountShieldManager().sendSocketMessage(auth);
        } catch (IOException ex) {}
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent e) {
        if(!v4GuardCore.getInstance().isAccountShieldFound()) {
            Player player = e.getPlayer();
            if (player.isOnlineMode()) {
                Authentication auth = new Authentication(player.getUsername(), AuthType.MOJANG);
                v4GuardCore.getInstance().getAccountShieldManager().sendSocketMessage(auth);
            }
        }
    }

}
