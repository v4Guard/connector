package io.v4guard.plugin.velocity.messager;


import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.kyori.adventure.text.Component;

public class Messager {

    public void broadcastWithPermission(String message, String permission){
        if(permission.equals("*")){
            for(Player player : v4GuardVelocity.getV4Guard().getServer().getAllPlayers()){
                player.sendMessage(Component.text(message));
            }
        } else{
            for(Player player : v4GuardVelocity.getV4Guard().getServer().getAllPlayers()){
                if(player.hasPermission(permission)){
                    player.sendMessage(Component.text(message));
                }
            }
        }
    }

}
