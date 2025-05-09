package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.compatibility.Messenger;
import io.v4guard.connector.api.constants.ListenersConstants;

import java.util.List;

public class ChatMessageListener implements Emitter.Listener {

    private final CoreInstance coreInstance = CoreInstance.get();

    @Override
    public void call(Object... args) {
        JsonNode request;

        try {
            request = CoreInstance.get().getObjectMapper().readTree(args[0].toString());
        } catch (JsonProcessingException e) {
            UnifiedLogger.get().severe("Error parsing message: " + e.getMessage());
            return;
        }

        String permission = request.get("permission").asText(ListenersConstants.NO_PERMISSION_NEEDED);
        List<String> players = coreInstance.getObjectMapper().convertValue(request.get("players"), new TypeReference<>() {});
        String message = request.get("message").asText();

        Messenger messenger = coreInstance.getPlugin().getMessenger();

        if (!permission.equals(ListenersConstants.NO_PERMISSION_NEEDED)) {
            messenger.broadcastWithPermission(message, permission);
        } else for (String player : players) {
            messenger.sendMessageTo(player, message);
        }
    }
}
