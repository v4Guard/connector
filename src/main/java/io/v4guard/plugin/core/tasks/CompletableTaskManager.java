package io.v4guard.plugin.core.tasks;

import io.v4guard.plugin.core.tasks.common.CompletableTask;
import io.v4guard.plugin.core.tasks.types.CompletableMCBrandTask;

import java.util.HashMap;

public class CompletableTaskManager {

    private HashMap<String, CompletableTask> tasks = new HashMap<>();

    public HashMap<String, CompletableTask> getTasks() {
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