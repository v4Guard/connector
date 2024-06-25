package io.v4guard.connector.common.socket.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.socket.emitter.Emitter;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.vpn.VPNCallbackTask;


public class CheckListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        ObjectNode doc;
        try {
            doc = CoreInstance.get().getObjectMapper().readValue(args[0].toString(), ObjectNode.class);
        } catch (JsonProcessingException e) {
            UnifiedLogger.get().severe("Failed to parse check response: " + e.getMessage());
            return;
        }
        VPNCallbackTask task = (VPNCallbackTask) CoreInstance.get().getPendingTasks().get(doc.get("taskID").asText());

        if (task != null) {
            task.setData(doc);
            task.complete();
        }
    }
}