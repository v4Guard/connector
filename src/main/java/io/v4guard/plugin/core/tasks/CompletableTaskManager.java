package io.v4guard.plugin.core.tasks;

import io.v4guard.plugin.core.tasks.common.CompletableTask;
import io.v4guard.plugin.core.tasks.types.CompletableMCBrandTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CompletableTaskManager {

    private final Map<String, CompletableTask> tasks = new ConcurrentHashMap<>();

    public Map<String, CompletableTask> getTasks() {
        return this.tasks;
    }

    public CompletableMCBrandTask getBrandTask(String username){
        for(CompletableTask task : this.tasks.values()){
            if(task instanceof CompletableMCBrandTask){
                CompletableMCBrandTask brandTask = (CompletableMCBrandTask) task;
                if(brandTask.getUsername().equals(username)){
                    return brandTask;
                }
            }
        }
        return null;
    }

}