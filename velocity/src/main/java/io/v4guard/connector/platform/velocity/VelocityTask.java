package io.v4guard.connector.platform.velocity;

import com.velocitypowered.api.scheduler.ScheduledTask;
import io.v4guard.connector.common.compatibility.UniversalTask;

public class VelocityTask implements UniversalTask {

    private final ScheduledTask TASK;

    public VelocityTask(ScheduledTask task) {
        this.TASK = task;
    }

    @Override
    public void cancel() {
        TASK.cancel();
    }
}
