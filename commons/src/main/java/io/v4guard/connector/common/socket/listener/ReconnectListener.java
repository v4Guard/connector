package io.v4guard.connector.common.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.connector.common.socket.ActiveConnection;

public class ReconnectListener implements Emitter.Listener {

    private final ActiveConnection backend;

    public ReconnectListener(ActiveConnection backend) {
        this.backend = backend;
    }

    @Override
    public void call(Object... args) {
        backend.reconnect();
    }
}
