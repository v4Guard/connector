package io.v4guard.plugin.spigot.messager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Messager {

    public static void broadcastWithPermission(String message, String permission){
        if(permission.equals("*")){
            for(Player player : Bukkit.getServer().getOnlinePlayers()){
                player.sendMessage(message);
            }
        } else{
            for(Player player : Bukkit.getServer().getOnlinePlayers()){
                if(player.hasPermission(permission)){
                    player.sendMessage(message);
                }
            }
        }
    }

    public void sendToPlayer(String message, String username){
        Player player = Bukkit.getServer().getPlayer(username);
        if(player != null){
            player.sendMessage(message);
        }
    }

}
