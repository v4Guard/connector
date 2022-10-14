package io.v4guard.plugin.bungee.accounts;

import io.v4guard.plugin.core.accounts.auth.Authentication;
import io.v4guard.plugin.core.accounts.messaging.MessageReceiver;
import io.v4guard.plugin.core.v4GuardCore;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BungeeMessageReceiver extends MessageReceiver implements Listener {

    public BungeeMessageReceiver(Plugin plugin) {
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
        if(!MessageReceiver.CHANNEL.equals("BungeeCord")) {
            plugin.getProxy().registerChannel(MessageReceiver.CHANNEL);
        }
    }

    @EventHandler
    public void onMessage(PluginMessageEvent e){
        if (!e.getTag().equals(MessageReceiver.CHANNEL)) {
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
