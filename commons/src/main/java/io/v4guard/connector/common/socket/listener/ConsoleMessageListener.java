package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;

import java.util.logging.Level;

public class ConsoleMessageListener implements Emitter.Listener {

    private final CoreInstance coreInstance;

    public ConsoleMessageListener(CoreInstance coreInstance) {
        this.coreInstance = coreInstance;
    }

    @Override
    public void call(Object... args) {
        JsonNode request;

        try {
            request = coreInstance.getObjectMapper().readTree(args[0].toString());
        } catch (JsonProcessingException e) {
            UnifiedLogger.get().log(Level.SEVERE, "Failed to parse console message", e);
            return;
        }

        String message = request.get("message").asText(null);

        Level lvl = Level.INFO;

        try {
            lvl = Level.parse(request.get("level").asText("INFO").toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        UnifiedLogger.get().log(lvl, message);
    }

}