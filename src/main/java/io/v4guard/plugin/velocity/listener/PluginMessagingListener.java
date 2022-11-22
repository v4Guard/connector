package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.md_5.bungee.api.plugin.Listener;
import org.bson.Document;

public class PluginMessagingListener implements Listener {

    @Subscribe(order = PostOrder.FIRST)
    public void onMessage(PluginMessageEvent e){
        Document privacySettings = (Document) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
        boolean invalidatedCache = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);
        if(!invalidatedCache && privacySettings.getBoolean("collectMCBrand", true)){
            if(e.getSource() instanceof Player){
                if(e.getIdentifier().getId().equals("MC|Brand") || e.getIdentifier().getId().equals("minecraft:brand")){
                    Player player = (Player) e.getSource();
                    Document data = new Document();
                    data.put("username", player.getUsername());
                    data.put("brand", new String(e.getData()));
                    v4GuardVelocity.getCoreInstance().getBackendConnector().getSocket().emit("mc:brand", data.toJson());
                } else if (e.getIdentifier().getId().equals("LMC") || e.getIdentifier().getId().equals("labymod3:main")){
                    Player player = (Player) e.getSource();
                    Document data = new Document();
                    data.put("username", player.getUsername());
                    data.put("brand", "labymod");
                    v4GuardVelocity.getCoreInstance().getBackendConnector().getSocket().emit("mc:brand", data.toJson());
                }
            }
        }
    }
}
