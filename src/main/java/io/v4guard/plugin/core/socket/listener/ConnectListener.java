package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.socket.Backend;

public class ConnectListener implements Emitter.Listener {

    private Backend backend;

    public ConnectListener(Backend backend) {
        this.backend = backend;
    }

    @Override
    public void call(Object... args) {
        //v4GuardCore.getInstance().getLogger().log(Level.INFO,"socket.on(connect)");
        backend.initializeListeners();
    }
}
