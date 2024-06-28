package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.CheckStatus;

public class CleanCacheListener implements Emitter.Listener {

    private final CoreInstance backend;

    public CleanCacheListener(CoreInstance coreInstance) {
        this.backend = coreInstance;
    }

    @Override
    public void call(Object... args) {
        JsonNode json;

        try {
            json = backend.getObjectMapper().readTree(args[0].toString());
        } catch (JsonProcessingException e) {
            UnifiedLogger.get().severe("Failed to parse clean cache message: " + e.getMessage());
            return;
        }

        if (json.has("username")) {
            backend.getCheckDataCache().cleanup(json.get("username").asText());
        } else {
            backend.getCheckDataCache().invalidateAllThatNot(CheckStatus.WAITING);
        }
    }

}