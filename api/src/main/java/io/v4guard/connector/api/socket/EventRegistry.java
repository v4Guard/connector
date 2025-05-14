package io.v4guard.connector.api.socket;

import java.util.Collection;

public interface EventRegistry {
    Collection<SocketMessageListener> getRegisteredEvent();

    void registerListener(final SocketMessageListener event);

    void unregisterListener(final SocketMessageListener event);
}
