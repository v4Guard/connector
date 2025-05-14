package io.v4guard.connector.api.socket;

import java.util.function.Consumer;

public abstract class SocketMessageListener {

    private final String eventName;

    public SocketMessageListener(String eventName) {
        this.eventName = eventName;
    }

    public abstract Consumer<Object[]> onEvent();

    public String getEventName() {
        return eventName;
    }

}
