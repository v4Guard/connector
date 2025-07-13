package io.v4guard.connector.api;

import io.v4guard.connector.api.socket.ActiveSettings;
import io.v4guard.connector.api.socket.Connection;
import io.v4guard.connector.api.socket.EventRegistry;

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
     * Get the event registery of the connection to v4Guard server.
     * @return the event registery
     */
    EventRegistry getEventRegistry();

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

    /**
     * Set the event registery of the connection to v4Guard server.
     * @param eventRegistry the event registery
     */
    void setEventRegistery(EventRegistry eventRegistry);

}
