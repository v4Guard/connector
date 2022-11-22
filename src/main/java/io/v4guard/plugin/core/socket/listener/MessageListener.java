package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MessageListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public MessageListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        String permission = doc.getOrDefault("permission", "*").toString();
        List<String> players = (List<String>) doc.getOrDefault("players", new ArrayList<>());
        String message = (String) doc.getOrDefault("message", "Disconnected");
        switch (v4GuardCore.getInstance().getPluginMode()){
            case BUNGEE: {
                List<String> broadcasted = v4GuardBungee.getV4Guard().getMessager().broadcastWithPermission(message, permission);
                players.removeAll(broadcasted);
                for(String player : players){
                    v4GuardBungee.getV4Guard().getMessager().sendToPlayer(message, player);
                }
                break;
            }

            case VELOCITY: {
                List<String> broadcasted = v4GuardVelocity.getV4Guard().getMessager().broadcastWithPermission(message, permission);
                players.removeAll(broadcasted);
                for(String player : players){
                    v4GuardVelocity.getV4Guard().getMessager().sendToPlayer(message, player);
                }
                break;
            }

        }

    }
}
