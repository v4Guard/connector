package io.v4guard.plugin.core.check.vpn;

import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.BlockReason;
import io.v4guard.plugin.core.check.CallbackTask;
import io.v4guard.plugin.core.check.CheckStatus;
import io.v4guard.plugin.core.check.PlayerCheckData;
import io.v4guard.plugin.core.constants.VPNCheckConstants;
import io.v4guard.plugin.core.socket.RemoteSettings;
import io.v4guard.plugin.core.utils.StringUtils;
import org.bson.Document;

import java.util.ArrayList;

public class VPNCallbackTask extends CallbackTask {
    private Document data;
    private final long startedAt;

    public VPNCallbackTask(PlayerCheckData playerCheckData) {
        super(playerCheckData);

        this.startedAt = System.currentTimeMillis();
    }

    public void start() {
        Document doc = new Document();

        doc.append(VPNCheckConstants.TASK_ID, this.taskID)
                .append(VPNCheckConstants.IP, this.checkData.getAddress())
                .append(VPNCheckConstants.USERNAME, this.checkData.getUsername())
                .append(VPNCheckConstants.VERSION, this.checkData.getVersion())
                .append(VPNCheckConstants.TIMESTAMP, this.startedAt)
                .append(VPNCheckConstants.VIRTUAL_HOST, this.checkData.getVirtualHost());

        CoreInstance.get().getPendingTasks().assign(this.taskID, this);
        CoreInstance.get().getBackend().send(VPNCheckConstants.CHECK_COMMAND, doc);
    }

    public void setData(Document data) {
        this.data = data;
    }

    public boolean hasData() {
        return this.data != null;
    }

    private boolean isBlocked() {
        if (!this.hasData()) {
            throw new UnsupportedOperationException("Task is not completed yet");
        }

        return this.data.getBoolean(VPNCheckConstants.BLOCK);
    }

    public long getStartedAt() {
        return this.startedAt;
    }

    @Override
    public void complete() {
        CoreInstance.get().getPendingTasks().remove(this.taskID);

        if (this.data.containsKey("message")) {
            String reason = StringUtils.buildMultilineString(this.data.get("message", new ArrayList<>()));
            checkData.setKickReason(StringUtils.replacePlaceholders(reason, (Document) this.data.get("variables")));
        } else {
            checkData.setKickReason(StringUtils.replacePlaceholders(RemoteSettings.getMessage("kick"), (Document) this.data.get("variables")));
        }

        checkData.setCheckStatus(isBlocked() ? CheckStatus.USER_DENIED : CheckStatus.USER_ALLOWED);

        if (this.data.containsKey("blockReason")) {
            checkData.setBlockReason(BlockReason.valueOf(this.data.getString("blockReason")));
        }

        super.complete();
    }
}
