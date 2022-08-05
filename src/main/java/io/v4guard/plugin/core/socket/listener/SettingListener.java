package io.v4guard.plugin.core.socket.listener;

import io.v4guard.plugin.core.socket.BackendConnector;
import io.socket.emitter.Emitter;
import org.bson.Document;

public class SettingListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public SettingListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        backendConnector.getSettings().put(doc.getString("key"), doc.get("value"));
    }
}
