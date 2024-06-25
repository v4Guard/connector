package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.utils.StringUtils;

import java.util.List;

public class KickListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        CoreInstance coreInstance = CoreInstance.get();
        JsonNode request = coreInstance.readTree(args[0].toString());

        String username = request.get("username").asText();
        List<String> rawReason = coreInstance.getObjectMapper().convertValue(request.get("message"), new TypeReference<>() {});
        String reason = StringUtils.buildMultilineString(rawReason);

        coreInstance.getPlugin().kickPlayer(username, reason);
    }
}
