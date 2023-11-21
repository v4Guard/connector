package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.socket.Backend;

public class ReconnectListener implements Emitter.Listener {

    private Backend backend;

    public ReconnectListener(Backend backend) {
        this.backend = backend;
    }

    @Override
    public void call(Object... args) {
        backend.reconnect();
    }
}
