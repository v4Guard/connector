package io.v4guard.connector.api;

public class v4GuardConnectorProvider {
    private static ConnectorAPI connectorAPI;

    private v4GuardConnectorProvider() {
        throw new UnsupportedOperationException("An utility class cannot be instantiated!");
    }

    public static ConnectorAPI get() {
        ConnectorAPI connectorApi = v4GuardConnectorProvider.connectorAPI;
        if (connectorAPI == null) {
            throw new NotInitializedException();
        }
        return connectorApi;
    }

    public static void register(ConnectorAPI connectorAPI) {
        v4GuardConnectorProvider.connectorAPI = connectorAPI;
    }

    public static void unregister() {
        v4GuardConnectorProvider.connectorAPI = null;
    }

    public static class NotInitializedException extends IllegalStateException {
        private static final String message = """
                    The v4Guard Connector API isn't initialized yet!
                    This is most likely because the v4Guard Connector isn't loaded or you're trying to use it before the plugin is enabled.
                    Please make sure to enable the v4Guard Connector plugin before using the API.
                """;

        public NotInitializedException() {
            super(message);
        }

    }
}
