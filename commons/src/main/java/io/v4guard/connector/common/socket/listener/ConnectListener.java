package io.v4guard.connector.common.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.connector.common.socket.Connection;

public class ConnectListener implements Emitter.Listener {

    private final Connection remoteConnection;

    public ConnectListener(Connection backend) {
        this.remoteConnection = backend;
    }

    @Override
    public void call(Object... args) {
        remoteConnection.initializeListeners();
    }
}
