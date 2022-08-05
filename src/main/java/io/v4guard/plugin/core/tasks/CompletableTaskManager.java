package io.v4guard.plugin.core.tasks;

import io.v4guard.plugin.core.tasks.common.CompletableTask;

import java.util.HashMap;

public class CompletableTaskManager {

    private HashMap<String, CompletableTask> tasks = new HashMap();

    public HashMap<String, CompletableTask> getTasks() {
        return this.tasks;
    }

}