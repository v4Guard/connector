package io.v4guard.plugin.bungee.messager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class Messager {

    public List<String> broadcastWithPermission(String message, String permission){
        List<String> broadcastedPlayers = new ArrayList<>();
        if(permission.equals("*")){
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
                player.sendMessage(new ComponentBuilder(message).create());
                broadcastedPlayers.add(player.getName());
            }
        } else{
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
                if(player.hasPermission(permission)){
                    player.sendMessage(new ComponentBuilder(message).create());
                    broadcastedPlayers.add(player.getName());
                }
            }
        }
        return broadcastedPlayers;
    }

    public void sendToPlayer(String message, String username){
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(username);
        if(player != null){
            player.sendMessage(new ComponentBuilder(message).create());
        }
    }

}
