package io.v4guard.plugin.velocity.accounts;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.v4guard.plugin.core.accounts.auth.Authentication;
import io.v4guard.plugin.core.accounts.messaging.MessageReceiver;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.md_5.bungee.api.plugin.Listener;
import org.bson.Document;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class VelocityMessageReceiver extends MessageReceiver implements Listener {

    public VelocityMessageReceiver(v4GuardVelocity plugin) {
        plugin.getServer().getEventManager().register(plugin, this);
        plugin.getServer().getChannelRegistrar().register(new LegacyChannelIdentifier(MessageReceiver.CHANNEL));
        plugin.getServer().getChannelRegistrar().register(MinecraftChannelIdentifier.from(MessageReceiver.CHANNEL));
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onMessage(PluginMessageEvent e){
        if (!e.getIdentifier().getId().equals(MessageReceiver.CHANNEL)) {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(e.getData()));
        try {
            String data = in.readUTF();
            Document doc = Document.parse(data);
            Authentication auth = Authentication.deserialize(doc);
            v4GuardCore.getInstance().getAccountShieldManager().sendSocketMessage(auth);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
