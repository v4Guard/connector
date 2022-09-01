package io.v4guard.plugin.spigot;

import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.check.common.CheckStatus;
import io.v4guard.plugin.core.check.common.VPNCheck;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.StringUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SpigotCheckProcessor implements CheckProcessor {

    @Override
    public void onPreLogin(String username, Object event) {
        AsyncPlayerPreLoginEvent e = (AsyncPlayerPreLoginEvent) event;

        //Clear other checks if player changes his address
        String address = e.getAddress().getHostAddress();
        v4GuardSpigot.getCoreInstance().getCheckManager().cleanupChecks(username);

        if(v4GuardSpigot.getCoreInstance().getBackendConnector() == null|| v4GuardSpigot.getCoreInstance().getBackendConnector().getSettings() == null) return;
        final boolean wait = (boolean) v4GuardSpigot.getCoreInstance().getBackendConnector().getSettings().getOrDefault("waitResponse", false);;
        new CompletableNameCheckTask(e.getName()) {
            @Override
            public void complete(boolean nameIsValid) {
                if(nameIsValid){
                    new CompletableIPCheckTask(address, e.getName(), -1, "spigot") {
                        @Override
                        public void complete() {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    VPNCheck check = getCheck();
                                    if(check.getStatus() == CheckStatus.USER_DENIED){
                                        if(wait){
                                            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                                            e.setKickMessage(check.getReason());
                                        } else {
                                            //Try kick player if is online
                                            onExpire(check);
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
                    Document kickMessages = (Document) v4GuardSpigot.getCoreInstance().getBackendConnector().getSettings().getOrDefault("waitResponse", false);;
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
    public void onPostLogin(String username, Object event) {
        VPNCheck check = v4GuardSpigot.getCoreInstance().getCheckManager().getCheckStatus(username);
        if(check == null) return;
        check.setPostLoginTime(System.currentTimeMillis());
        if(check.getStatus() == CheckStatus.USER_DENIED){
            PlayerJoinEvent e = (PlayerJoinEvent) event;
            e.getPlayer().kickPlayer(check.getReason());
        }
    }

    @Override
    public boolean onExpire(VPNCheck status) {
        Player p = Bukkit.getPlayer(status.getName());
        if(status.getStatus() == CheckStatus.USER_DENIED && p != null){
            new BukkitRunnable() {
                @Override
                public void run() {
                    p.kickPlayer(status.getReason());
                }
            }.runTask(v4GuardSpigot.getV4Guard());
            return true;
        }
        return false;
    }

    @Override
    public void kickPlayer(String username, String reason){
       Player player = Bukkit.getPlayer(username);
        if (player != null) {
            player.kickPlayer(reason);
        }
    }
}
