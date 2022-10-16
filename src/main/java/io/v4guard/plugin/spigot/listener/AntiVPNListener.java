package io.v4guard.plugin.spigot.listener;

import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class AntiVPNListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(final AsyncPlayerPreLoginEvent e) {
        v4GuardCore.getInstance().getCheckManager().runPreLoginCheck(e.getName(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostLogin(PlayerJoinEvent e) {
        v4GuardCore.getInstance().getCheckManager().runPostLoginCheck(e.getPlayer().getName(), e);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        if(v4GuardCore.getInstance().getChatFilterManager().canLookupMessage(e.getMessage())){
            Document data = new Document();
            Player player = e.getPlayer();
            data.put("username", player.getName());
            data.put("location", v4GuardCore.getInstance().getCheckManager().getProcessors().get(0).getPlayerServer(player.getName()));
            data.put("message", e.getMessage());
            v4GuardCore.getInstance().getBackendConnector().getSocket().emit("chatfilter:chat", data.toJson());
        }
    }

}
