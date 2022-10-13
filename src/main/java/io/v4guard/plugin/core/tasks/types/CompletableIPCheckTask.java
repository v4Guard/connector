package io.v4guard.plugin.core.tasks.types;

import io.v4guard.plugin.core.check.common.CheckStatus;
import io.v4guard.plugin.core.check.common.VPNCheck;
import io.v4guard.plugin.core.tasks.common.CompletableTask;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public abstract class CompletableIPCheckTask implements CompletableTask {
    private final String taskID;
    private final String address;
    private final String username;
    private int version;
    private Document data;
    private VPNCheck check;

    public CompletableIPCheckTask(String address, String username, int version, String virtualHost) {
        this.address = address;
        this.username = username;
        this.version = version;
        this.taskID = UUID.randomUUID().toString();
        this.data = new Document();
        v4GuardCore.getInstance().getCompletableTaskManager().getTasks().put(this.taskID, this);
        Document doc = new Document();
        doc.put("taskID", this.taskID);
        doc.put("ip", this.address);
        doc.put("username", this.username);
        doc.put("version", this.version);
        doc.put("timestamp", System.currentTimeMillis());
        doc.put("virtualHost", virtualHost);
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
            this.check = v4GuardCore.getInstance().getCheckManager().buildCheckStatus(this.getUsername(), this.getAddress());
            this.replacePlaceholders(check);
            check.setStatus(isBlocked() ? CheckStatus.USER_DENIED : CheckStatus.USER_ALLOWED);
            v4GuardCore.getInstance().getCheckManager().getCheckStatusMap().put(username, check);
            this.complete();
            v4GuardCore.getInstance().getCompletableTaskManager().getTasks().remove(this.getTaskID());
        }
    }

    public boolean isCompleted() {
        return this.data.size() > 0;
    }

    public void addData(Document doc) {
        this.data = doc;
    }

    public Document getData() {
        return this.data;
    }

    public String getAddress() {
        return this.address;
    }

    public String getUsername() {
        return this.username;
    }

    public VPNCheck getCheck() {
        return check;
    }

    public void replacePlaceholders(VPNCheck status){
        status.setReason(StringUtils.replacePlaceholders(status.getReason(), (Document) getData().get("variables")));
    }
}
