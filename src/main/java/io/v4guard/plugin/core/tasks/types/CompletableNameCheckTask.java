package io.v4guard.plugin.core.tasks.types;

import io.v4guard.plugin.core.tasks.common.CheckCompletableTask;
import io.v4guard.plugin.core.tasks.common.CompletableTask;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public abstract class CompletableNameCheckTask implements CheckCompletableTask {

    private final String taskID;
    private final String username;

    private boolean usernameIsValid = true;

    public CompletableNameCheckTask(String username) {
        this.username = username;
        this.taskID = UUID.randomUUID().toString();
        Document nameValidator = (Document) v4GuardCore.getInstance().getBackendConnector().getSettings().get("nameValidator");
        if(nameValidator.getBoolean("isEnabled")){
            String regex = nameValidator.getString("regex");
            this.usernameIsValid = Pattern.compile("^" + regex + "$").matcher(username).matches();
        }
        complete(this.usernameIsValid);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getTaskID() {
        return this.taskID;
    }

}
