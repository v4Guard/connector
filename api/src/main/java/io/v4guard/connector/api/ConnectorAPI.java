package io.v4guard.connector.api;

import io.v4guard.connector.api.socket.ActiveSettings;
import io.v4guard.connector.api.socket.Connection;

public interface ConnectorAPI {

    /**
     * Get the socket connection to the v4Guard server.
     * @return the socket connection
     */
    Connection getConnection();

    /**
     * Get the settings of the active company.
     * @return the active settings
     */
    ActiveSettings getActiveSettings();

    /**
     * Set the socket connection to the v4Guard server.
     * @param connection the socket connection
     */
    void setConnection(Connection connection);

    /**
     * Set the settings of the active company.
     * @param activeSettings the active settings
     */
    void setActiveSettings(ActiveSettings activeSettings);

}
