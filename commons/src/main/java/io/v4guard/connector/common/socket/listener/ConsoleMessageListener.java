package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.utils.BannerMessage;

import java.util.ArrayList;
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

        Level lvl = Level.INFO;

        try {
            lvl = Level.parse(request.get("level").asText("INFO").toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        if (request.has("banner")) {
            BannerMessage.create()
                    .lines(coreInstance.getObjectMapper().convertValue(
                            request.get("lines"),
                            new TypeReference<ArrayList<String>>() {}
                    ))
                    .log(lvl);
        } else {
            String message = request.get("message").asText(null);
            UnifiedLogger.get().log(lvl, message);
        }
    }

}