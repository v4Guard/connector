package io.v4guard.plugin.spigot;

import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.CheckStatus;
import io.v4guard.plugin.core.utils.StringUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SpigotCheckProcessor implements CheckProcessor {

    @Override
    public void onPreLogin(String username, Object event) {
        AsyncPlayerPreLoginEvent e = (AsyncPlayerPreLoginEvent) event;
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
                                    String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("kick"));
                                    Document data = (Document) task.getData().get("result");
                                    kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, (Document) data.get("variables"));
                                    String username = e.getName();
                                    if(isBlocked()){
                                        if(wait){
                                            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                                            e.setKickMessage(kickReasonMessage);
                                        } else {
                                            v4GuardSpigot.getCoreInstance().getCheckManager().getCheckStatusMap().put(username, new CheckStatus(username, kickReasonMessage, true));
                                            Player p = Bukkit.getPlayer(username);
                                            if(p != null){
                                                p.kickPlayer(kickReasonMessage);
                                            }
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

    @Override
    public void onLogin(String username, Object event) {
        PlayerLoginEvent e = (PlayerLoginEvent) event;
        CheckStatus status = v4GuardSpigot.getCoreInstance().getCheckManager().getCheckStatus(e.getPlayer().getName());
        if(status != null && status.isBlocked()){
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, status.getReason());
            v4GuardSpigot.getCoreInstance().getCheckManager().getCheckStatusMap().remove(e.getPlayer().getName());
        }
    }

    @Override
    public void onPostLogin(String username, Object event) {
        PlayerJoinEvent e = (PlayerJoinEvent) event;
        CheckStatus status = v4GuardSpigot.getCoreInstance().getCheckManager().getCheckStatus(e.getPlayer().getName());
        if(status != null && status.isBlocked()){
            e.getPlayer().kickPlayer(status.getReason());
            v4GuardSpigot.getCoreInstance().getCheckManager().getCheckStatusMap().remove(e.getPlayer().getName());
        }
    }
}
