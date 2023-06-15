package io.v4guard.plugin.core.socket.listener.messaging;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.v4GuardCore;

public class MessageListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public MessageListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        switch (v4GuardCore.getInstance().getPluginMode()){
            case BUNGEE: {
                MessageListenerBungeeProcess.process(args);
                break;
            }
            case VELOCITY: {
                MessageListenerVelocityProcess.process(args);
                break;
            }
        }
    }
}
