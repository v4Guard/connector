package io.v4guard.connector.common.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.connector.common.socket.Connection;

public class ReconnectListener implements Emitter.Listener {

    private final Connection backend;

    public ReconnectListener(Connection backend) {
        this.backend = backend;
    }

    @Override
    public void call(Object... args) {
        backend.reconnect();
    }
}
