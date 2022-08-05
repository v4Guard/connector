package io.v4guard.plugin.bungee.messager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Messager {

    public void broadcastWithPermission(String message, String permission){
        if(permission.equals("*")){
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
                player.sendMessage(new ComponentBuilder(message).create());
            }
        } else{
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
                if(player.hasPermission(permission)){
                    player.sendMessage(new ComponentBuilder(message).create());
                }
            }
        }
    }

}
