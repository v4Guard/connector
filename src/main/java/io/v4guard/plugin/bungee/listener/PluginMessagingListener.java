package io.v4guard.plugin.bungee.listener;

import io.v4guard.plugin.bungee.v4GuardBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

public class PluginMessagingListener implements Listener {

    public PluginMessagingListener() {
        v4GuardBungee.getV4Guard().getProxy().getPluginManager().registerListener(v4GuardBungee.getV4Guard(), this);
    }

    @EventHandler
    public void onChannelMessage(PluginMessageEvent e){
        Document privacySettings = (Document) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
        boolean invalidatedCache = (boolean) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);
        if(!invalidatedCache && privacySettings.getBoolean("collectMCBrand", true)){
            if(e.getSender() instanceof ProxiedPlayer){
                if(e.getTag().equals("MC|Brand") || e.getTag().equals("minecraft:brand")){
                    ProxiedPlayer player = (ProxiedPlayer) e.getSender();
                    Document data = new Document();
                    data.put("username", player.getName());
                    data.put("brand", new String(e.getData()));
                    v4GuardBungee.getCoreInstance().getBackendConnector().getSocket().emit("mc:brand", data.toJson());
                } else if (e.getTag().equals("LMC") || e.getTag().equals("labymod3:main")){
                    ProxiedPlayer player = (ProxiedPlayer) e.getSender();
                    Document data = new Document();
                    data.put("username", player.getName());
                    data.put("brand", "labymod");
                    v4GuardBungee.getCoreInstance().getBackendConnector().getSocket().emit("mc:brand", data.toJson());
                }
            }
        }
    }
}
