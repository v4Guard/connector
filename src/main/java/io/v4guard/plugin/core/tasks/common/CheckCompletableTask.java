package io.v4guard.plugin.core.tasks.common;

public interface CheckCompletableTask {

    void complete(boolean bool);
    String getTaskID();

}