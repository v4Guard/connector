package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.PlayerFetchResult;

public class FindListener implements Emitter.Listener {

    private final CoreInstance backend;

    public FindListener(CoreInstance backend) {
        this.backend = backend;
    }

    @Override
    public void call(Object... args) {
        JsonNode doc;
        try {
            doc = backend.getObjectMapper().readTree(args[0].toString());
        } catch (JsonProcessingException e) {
            UnifiedLogger.get().severe("Error parsing find message: " + e.getMessage());
            return;
        }

        if (doc.has("username")) {
            String taskID = doc.get("taskID").asText();
            String username = doc.get("username").asText();
            PlayerFetchResult<?> fetchResult = backend.getPlugin().fetchPlayer(username);

            if (fetchResult.getServerName() != null) {
                ObjectNode findRequest = backend.getObjectMapper().createObjectNode();
                findRequest.put("taskID", taskID);
                findRequest.put("username", username);
                findRequest.put("location", fetchResult.getServerName());

                backend.getRemoteConnection().send("find", findRequest);
            }
        }
    }

}
