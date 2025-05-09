package io.v4guard.connector.common.api;

import io.v4guard.connector.api.ConnectorAPI;
import io.v4guard.connector.api.socket.Connection;

public class DefaultConnectorAPI implements ConnectorAPI {

    private Connection connection;

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
