package io.v4guard.plugin.velocity.messager;


import com.velocitypowered.api.proxy.Player;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Messager {

    public List<String> broadcastWithPermission(String message, String permission){
        List<String> broadcastedPlayers = new ArrayList<>();
        if(permission.equals("*")){
            for(Player player : v4GuardVelocity.getV4Guard().getServer().getAllPlayers()){
                player.sendMessage(Component.text(message));
                broadcastedPlayers.add(player.getUsername());
            }
        } else{
            for(Player player : v4GuardVelocity.getV4Guard().getServer().getAllPlayers()){
                if(player.hasPermission(permission)){
                    player.sendMessage(Component.text(message));
                    broadcastedPlayers.add(player.getUsername());
                }
            }
        }
        return broadcastedPlayers;
    }

    public void sendToPlayer(String message, String username){
        Optional<Player> player = v4GuardVelocity.getV4Guard().getServer().getPlayer(username);
        if(player.isPresent()){
            player.get().sendMessage(Component.text(message));
        }
    }

}
