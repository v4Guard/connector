package io.v4guard.plugin.core.check;

import java.util.UUID;

public class CallbackTask {

    protected String taskID;
    protected PlayerCheckData checkData;

    public CallbackTask(String taskID, PlayerCheckData playerCheckData) {
        this.taskID = taskID;
        this.checkData = playerCheckData;
    }

    public CallbackTask(PlayerCheckData playerCheckData) {
        this(UUID.randomUUID().toString(), playerCheckData);
    }

    public void start() {

    }

    public void complete() {
        checkData.triggerTaskCompleted();
    }

    public String getTaskID() {
        return this.taskID;
    }

}
