package io.v4guard.plugin.spigot.messager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Messager {

    public List<String> broadcastWithPermission(String message, String permission){
        List<String> broadcastedPlayers = new ArrayList<>();
        if(permission.equals("*")){
            for(Player player : Bukkit.getServer().getOnlinePlayers()){
                player.sendMessage(message);
                broadcastedPlayers.add(player.getName());
            }
        } else{
            for(Player player : Bukkit.getServer().getOnlinePlayers()){
                if(player.hasPermission(permission)){
                    player.sendMessage(message);
                    broadcastedPlayers.add(player.getName());
                }
            }
        }
        return broadcastedPlayers;
    }

    public void sendToPlayer(String message, String username){
        Player player = Bukkit.getServer().getPlayer(username);
        if(player != null){
            player.sendMessage(message);
        }
    }

}
