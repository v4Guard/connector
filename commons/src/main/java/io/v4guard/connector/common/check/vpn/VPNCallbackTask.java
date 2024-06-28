package io.v4guard.connector.common.check.vpn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.BlockReason;
import io.v4guard.connector.common.check.CallbackTask;
import io.v4guard.connector.common.check.CheckStatus;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.common.constants.VPNCheckConstants;
import io.v4guard.connector.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class VPNCallbackTask extends CallbackTask {

    private final ObjectMapper objectMapper = CoreInstance.get().getObjectMapper();
    private ObjectNode data;

    public VPNCallbackTask(PlayerCheckData playerCheckData) {
        super(playerCheckData);
    }

    public void start() {
        super.start();

        data = CoreInstance.get().getObjectMapper().createObjectNode();
        data.put(VPNCheckConstants.TASK_ID, this.taskID)
                .put(VPNCheckConstants.IP, this.checkData.getAddress())
                .put(VPNCheckConstants.USERNAME, this.checkData.getUsername())
                .put(VPNCheckConstants.VERSION, this.checkData.getVersion())
                .put(VPNCheckConstants.BEDROCK, this.checkData.isBedrock())
                .put(VPNCheckConstants.TIMESTAMP, super.startedAt)
                .put(VPNCheckConstants.VIRTUAL_HOST, this.checkData.getVirtualHost());

        CoreInstance.get().getPendingTasks().assign(this.taskID, this);
        CoreInstance.get().getRemoteConnection().send(VPNCheckConstants.CHECK_COMMAND, data);
    }

    public void setData(ObjectNode data) {
        this.data = data;
    }

    public boolean hasData() {
        return this.data != null;
    }

    private boolean isBlocked() {
        if (!this.hasData()) {
            throw new IllegalStateException("Task is not completed yet");
        }

        return this.data.get(VPNCheckConstants.BLOCK).asBoolean();
    }

    @Override
    public void complete() {
        CoreInstance backend = CoreInstance.get();
        backend.getPendingTasks().remove(this.taskID);

        HashMap<String, String> variables = objectMapper.convertValue(this.data.get("variables"), new TypeReference<>() {});

        String reason;

        if (this.data.has("message")) {
            reason = StringUtils.buildMultilineString(objectMapper.convertValue(
                    this.data.get("message"),
                    new TypeReference<ArrayList<String>>() {})
            );
            checkData.setKickReason(StringUtils.replacePlaceholders(reason, variables));
        } else {
            reason = StringUtils.buildMultilineString(backend.getActiveSettings().getMessages().get("kick"));
            checkData.setKickReason(StringUtils.replacePlaceholders(reason, variables));
        }

        checkData.setCheckStatus(isBlocked() ? CheckStatus.USER_DENIED : CheckStatus.USER_ALLOWED);

        if (this.data.has("blockReason")) {
            checkData.setBlockReason(BlockReason.valueOf(this.data.get("blockReason").asText()));
        }

        super.complete();
    }
}
