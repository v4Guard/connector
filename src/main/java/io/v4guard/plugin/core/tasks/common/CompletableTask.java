package io.v4guard.plugin.core.tasks.common;

public interface CompletableTask {

    void complete();
    String getTaskID();

}