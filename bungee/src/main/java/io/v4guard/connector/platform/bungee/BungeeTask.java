package io.v4guard.connector.platform.bungee;

import io.v4guard.connector.common.compatibility.UniversalTask;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class BungeeTask implements UniversalTask {
    private final ScheduledTask TASK;

    public BungeeTask(ScheduledTask task) {
        this.TASK = task;
    }

    @Override
    public void cancel() {
        TASK.cancel();
    }
}
