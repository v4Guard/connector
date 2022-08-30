package io.v4guard.plugin.velocity.messager;


import com.velocitypowered.api.proxy.Player;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.kyori.adventure.text.Component;

import java.util.Optional;

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

    public void sendToPlayer(String message, String username){
        Optional<Player> player = v4GuardVelocity.getV4Guard().getServer().getPlayer(username);
        if(player.isPresent()){
            player.get().sendMessage(Component.text(message));
        }
    }

}
