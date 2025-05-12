package io.v4guard.connector.common.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.connector.common.socket.ActiveConnection;

public class ConnectListener implements Emitter.Listener {

    private final ActiveConnection remoteActiveConnection;

    public ConnectListener(ActiveConnection backend) {
        this.remoteActiveConnection = backend;
    }

    @Override
    public void call(Object... args) {
        remoteActiveConnection.initializeListeners();
    }
}
