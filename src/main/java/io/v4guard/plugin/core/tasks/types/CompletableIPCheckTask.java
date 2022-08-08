package io.v4guard.plugin.core.tasks.types;

import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.tasks.common.CompletableTask;
import io.v4guard.plugin.core.utils.CheckStatus;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class CompletableIPCheckTask implements CompletableTask {
    private final String taskID;
    private final String address;
    private final String username;
    private int version;
    private final ConcurrentHashMap<String, Object> data;
    private CheckStatus check;

    public CompletableIPCheckTask(String address, String username, int version) {
        this.address = address;
        this.username = username;
        this.version = version;
        this.taskID = UUID.randomUUID().toString();
        this.data = new ConcurrentHashMap();
        v4GuardCore.getInstance().getCompletableTaskManager().getTasks().put(this.taskID, this);
        Document doc = new Document();
        doc.put("taskID", this.taskID);
        doc.put("ip", this.address);
        doc.put("username", this.username);
        doc.put("version", this.version);
        doc.put("timestamp", System.currentTimeMillis());
        v4GuardCore.getInstance().getBackendConnector().send("check", doc);
        new Timer().schedule(new TimerTask(){

            @Override
            public void run() {
                if (!CompletableIPCheckTask.this.isCompleted()) {
                    Document doc = new Document();
                    doc.put("block", false);
                    CompletableIPCheckTask.this.addData(doc);
                    CompletableIPCheckTask.this.check();
                }
            }
        }, 5000L);
    }

    @Override
    public String getTaskID() {
        return this.taskID;
    }

    private boolean isBlocked() {
        if (!this.isCompleted()) throw new UnsupportedOperationException("Task is not completed yet");
        Document result = (Document)this.data.get("result");
        return result.getBoolean("block");
    }

    public String translateVariables(String reason) {
        Document variables = ((Document)this.data.get("result")).get("variables", Document.class);
        AtomicReference<String> result = new AtomicReference<>(reason);
        for (String key : variables.keySet()) {
            String value = variables.getString(key);
            result.set(result.get().replaceAll("\\{" + key + "}", value));
        }
        return result.get();
    }

    public void check() {
        if (this.isCompleted()) {
            this.check = v4GuardBungee.getCoreInstance().getCheckManager().buildCheckStatus(this.getUsername());
            this.replacePlaceholders(check);
            check.setBlocked(isBlocked());
            v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatusMap().put(username, check);
            this.complete();
            v4GuardCore.getInstance().getCompletableTaskManager().getTasks().remove(this.getTaskID());
        }
    }

    public boolean isCompleted() {
        return this.data.size() > 0;
    }

    public void addData(Object object) {
        this.data.put("result", object);
    }

    public ConcurrentHashMap<String, Object> getData() {
        return this.data;
    }

    public String getAddress() {
        return this.address;
    }

    public String getUsername() {
        return this.username;
    }

    public CheckStatus getCheck() {
        return check;
    }

    public void replacePlaceholders(CheckStatus status){
        Document data = (Document) this.getData().get("result");
        status.setReason(StringUtils.replacePlaceholders(status.getReason(), (Document) data.get("variables")));
    }
}
