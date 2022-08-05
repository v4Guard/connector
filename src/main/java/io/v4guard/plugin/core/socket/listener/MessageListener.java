package io.v4guard.plugin.core.socket.listener;

import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.spigot.v4GuardSpigot;
import io.socket.emitter.Emitter;
import org.bson.Document;

public class MessageListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public MessageListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        String permission = doc.getOrDefault("permission", "*").toString();
        switch (v4GuardCore.getInstance().getPluginMode()){
            case BUNGEE:
                v4GuardBungee.getV4Guard().getMessager().broadcastWithPermission(doc.getString("message"), permission);
                break;
            case SPIGOT:
                v4GuardSpigot.getV4Guard().getMessager().broadcastWithPermission(doc.getString("message"), permission);
                break;

        }

    }
}
