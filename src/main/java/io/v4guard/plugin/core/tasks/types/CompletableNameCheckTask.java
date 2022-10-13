package io.v4guard.plugin.core.tasks.types;

import io.v4guard.plugin.core.tasks.common.CheckCompletableTask;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

import java.util.UUID;
import java.util.regex.Pattern;

public abstract class CompletableNameCheckTask implements CheckCompletableTask {

    private final String taskID;
    private final String username;

    public CompletableNameCheckTask(String username) {
        this.username = username;
        this.taskID = UUID.randomUUID().toString();
        Document nameValidator = (Document) v4GuardCore.getInstance().getBackendConnector().getSettings().get("nameValidator");
        boolean usernameIsValid = true;
        if(nameValidator.getBoolean("isEnabled")){
            String regex = nameValidator.getString("regex");
            usernameIsValid = Pattern.compile("^" + regex + "$").matcher(username).matches();
        }
        complete(usernameIsValid);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getTaskID() {
        return this.taskID;
    }

}
