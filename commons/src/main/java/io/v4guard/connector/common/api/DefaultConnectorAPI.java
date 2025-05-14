package io.v4guard.connector.common.api;

import io.v4guard.connector.api.ConnectorAPI;
import io.v4guard.connector.api.socket.ActiveSettings;
import io.v4guard.connector.api.socket.Connection;
import io.v4guard.connector.api.socket.EventRegistry;

public class DefaultConnectorAPI implements ConnectorAPI {

    private Connection connection;

    private ActiveSettings activeSettings;

    private EventRegistry eventRegistry;

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public ActiveSettings getActiveSettings() {
        return activeSettings;
    }

    @Override
    public EventRegistry getEventRegistery() {
        return eventRegistry;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void setActiveSettings(ActiveSettings activeSettings) {
        this.activeSettings = activeSettings;
    }

    @Override
    public void setEventRegistery(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }
}
