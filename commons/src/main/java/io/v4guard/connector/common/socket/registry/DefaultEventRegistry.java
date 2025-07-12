package io.v4guard.connector.common.socket.registry;

import io.socket.emitter.Emitter;
import io.v4guard.connector.api.socket.EventRegistry;
import io.v4guard.connector.api.socket.SocketMessageListener;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.socket.ActiveConnection;
import io.v4guard.connector.common.socket.listener.CustomListener;

import java.util.Collection;
import java.util.HashMap;

public class DefaultEventRegistry implements EventRegistry {
    private final ActiveConnection activeConnection;
    private final HashMap<SocketMessageListener, Emitter.Listener> listeners;

    public DefaultEventRegistry() {
        this.activeConnection = CoreInstance.get().getRemoteConnection();
        this.listeners = new HashMap<>();
        CoreInstance.get().getConnectorAPI().setEventRegistery(this);
    }

    @Override
    public Collection<SocketMessageListener> getRegisteredEvent() {
        return listeners.keySet();
    }

    @Override
    public void registerListener(SocketMessageListener event) {
        CustomListener customListener = new CustomListener(event.onEvent());
        listeners.put(event, customListener);
        activeConnection.registerListener(event.getEventName(), customListener);
    }

    @Override
    public void unregisterListener(SocketMessageListener event) {
        activeConnection.unregisterListener(event.getEventName(), listeners.get(event));
        listeners.remove(event);
    }
}
