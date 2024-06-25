package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;

import java.util.logging.Level;

public class ConsoleMessageListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        JsonNode request;

        try {
            request = CoreInstance.get().getObjectMapper().readTree(args[0].toString());
        } catch (JsonProcessingException e) {
            UnifiedLogger.get().log(Level.SEVERE, "Failed to parse console message", e);
            return;
        }

        String message = request.get("message").asText(null);
        Level lvl = Level.parse(request.get("level").asText("INFO"));

        if (message == null) {
           return;
        }

        UnifiedLogger.get().log(lvl, message);
    }

}