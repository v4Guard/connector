package io.v4guard.plugin.core.socket.listener;

import io.v4guard.plugin.core.socket.BackendConnector;
import io.socket.emitter.Emitter;
import org.bson.Document;

public class SettingsListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public SettingsListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        for(String key : doc.keySet()) {
            backendConnector.getSettings().put(key, doc.get(key));
        }
    }
}
