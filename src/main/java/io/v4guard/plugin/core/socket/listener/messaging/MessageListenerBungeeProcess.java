package io.v4guard.plugin.core.socket.listener.messaging;

import io.v4guard.plugin.bungee.v4GuardBungee;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MessageListenerBungeeProcess {

    public static void process(Object... args) {
        Document doc = Document.parse(args[0].toString());
        String permission = doc.getOrDefault("permission", "no-permission").toString();
        List<String> players = (List<String>) doc.getOrDefault("players", new ArrayList<>());
        String message = (String) doc.getOrDefault("message", "Disconnected");
        if(!permission.equals("no-permission")){
            List<String> broadcasted = v4GuardBungee.getV4Guard().getMessager().broadcastWithPermission(message, permission);
            players.removeAll(broadcasted);
        }
        for(String player : players){
            v4GuardBungee.getV4Guard().getMessager().sendToPlayer(message, player);
        }

    }
}
