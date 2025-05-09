package io.v4guard.connector.api;

import io.v4guard.connector.api.socket.Connection;

public interface ConnectorAPI {

    /**
     * Get the socket connection to the v4Guard server.
     * @return the socket connection
     */
    Connection getConnection();

    /**
     * Set the socket connection to the v4Guard server.
     * @param connection the socket connection
     */
    void setConnection(Connection connection);

}
