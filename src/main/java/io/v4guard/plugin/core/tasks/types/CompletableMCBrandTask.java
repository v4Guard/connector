package io.v4guard.plugin.core.tasks.types;

import io.v4guard.plugin.core.tasks.common.CompletableTask;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

import java.util.*;

public class CompletableMCBrandTask implements CompletableTask {

    private final String taskID;
    private final String username;
    private final List<String> brands;

    public CompletableMCBrandTask(String username) {
        this.username = username;
        this.brands = new ArrayList<>();
        this.taskID = UUID.randomUUID().toString();
        v4GuardCore.getInstance().getCompletableTaskManager().getTasks().put(this.taskID, this);
        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                complete();
            }
        }, 1000L);
    }

    @Override
    public void complete() {
        Document data = new Document();
        data.put("username", username);
        data.put("brand", brands);
        v4GuardCore.getInstance().getBackendConnector().getSocket().emit("mc:brand", data.toJson());
        v4GuardCore.getInstance().getCompletableTaskManager().getTasks().remove(taskID);
    }

    @Override
    public String getTaskID() {
        return this.taskID;
    }

    public void addData(String brand) {
        if(!this.brands.contains(brand)) this.brands.add(brand);
    }

    public String getUsername() {
        return this.username;
    }

}
