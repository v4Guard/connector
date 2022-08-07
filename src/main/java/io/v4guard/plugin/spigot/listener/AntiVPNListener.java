package io.v4guard.plugin.spigot.listener;

import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.spigot.v4GuardSpigot;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AntiVPNListener implements Listener {

    @EventHandler
    public void onPreLogin(final AsyncPlayerPreLoginEvent e) {
        if (!v4GuardSpigot.getCoreInstance().getBackendConnector().getSocketStatus().equals(SocketStatus.AUTHENTICATED)) {
            return;
        }
        Document kickMessages = (Document) v4GuardSpigot.getCoreInstance().getBackendConnector().getSettings().get("messages");
        final boolean wait = (boolean) v4GuardSpigot.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        new CompletableNameCheckTask(e.getName()) {
            @Override
            public void complete(boolean nameIsValid) {
                if(nameIsValid){
                    new CompletableIPCheckTask(e.getAddress().getHostAddress(), e.getName(), -1) {
                        CompletableIPCheckTask task = this;
                        @Override
                        public void complete() {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    String username = e.getName();
                                    String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("kick"));
                                    Document data = (Document) task.getData().get("result");
                                    kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, (Document) data.get("variables"));
                                    if(isBlocked()){
                                        if(wait){
                                            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                                            e.setKickMessage(kickReasonMessage);
                                        } else {
                                            Player player = v4GuardSpigot.getV4Guard().getServer().getPlayer(e.getName());
                                            player.kickPlayer(kickReasonMessage);
                                        }
                                    }
                                }
                            }.runTask(v4GuardSpigot.getV4Guard());
                        }
                    };
                } else {
                    Player player = v4GuardSpigot.getV4Guard().getServer().getPlayer(e.getName());
                    if (player == null) {
                        return;
                    }
                    String message= StringUtils.buildMultilineString((List<String>) kickMessages.get("invalidUsername"));
                    message = StringUtils.replacePlaceholders(message, new Document("username", e.getName()));
                    player.kickPlayer(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        };

        try {
            if(wait) Thread.sleep(1000L);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

}
