package io.v4guard.plugin.core.check;

import io.v4guard.plugin.core.utils.TimestampUtils;

import java.time.Duration;
import java.util.UUID;

public class CallbackTask {

    protected String taskID;
    protected PlayerCheckData checkData;
    protected long startedAt;
    protected long maxExceutionTimeMillis = Duration.ofSeconds(5).toMillis();

    public CallbackTask(String taskID, PlayerCheckData playerCheckData) {
        this.taskID = taskID;
        this.checkData = playerCheckData;
        this.startedAt = -1L;
    }

    public CallbackTask(PlayerCheckData playerCheckData) {
        this(UUID.randomUUID().toString(), playerCheckData);
    }

    public void start() {
        this.startedAt = System.currentTimeMillis();
    }

    public void complete() {
        checkData.triggerTaskCompleted();
    }

    public String getTaskID() {
        return this.taskID;
    }

    public boolean isExpired() {
        if (this.startedAt == -1) {
            return false;
        }

        return TimestampUtils.isExpired(this.startedAt, maxExceutionTimeMillis);
    }

}
