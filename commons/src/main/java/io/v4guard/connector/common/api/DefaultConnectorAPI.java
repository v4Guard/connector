package io.v4guard.connector.common.api;

import io.v4guard.connector.api.ConnectorAPI;
import io.v4guard.connector.api.socket.ActiveSettings;
import io.v4guard.connector.api.socket.Connection;

public class DefaultConnectorAPI implements ConnectorAPI {

    private Connection connection;

    private ActiveSettings activeSettings;

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public ActiveSettings getActiveSettings() {
        return activeSettings;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void setActiveSettings(ActiveSettings activeSettings) {
        this.activeSettings = activeSettings;
    }
}
