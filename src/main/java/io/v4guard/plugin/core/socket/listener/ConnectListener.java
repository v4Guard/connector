package io.v4guard.plugin.core.socket.listener;

import io.v4guard.plugin.core.socket.BackendConnector;
import io.socket.emitter.Emitter;

import java.util.HashMap;

public class ConnectListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public ConnectListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        //v4GuardCore.getInstance().getLogger().log(Level.INFO,"socket.on(connect)");
        backendConnector.setSettings(new HashMap<>());
        backendConnector.handleEvents();
    }
}
