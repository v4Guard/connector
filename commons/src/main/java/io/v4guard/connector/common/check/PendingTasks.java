package io.v4guard.connector.common.check;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PendingTasks {

    private final Map<String, CallbackTask> TASKS = new ConcurrentHashMap<>();

    public CallbackTask get(String id) {
        return TASKS.get(id);
    }

    public CallbackTask remove(String id) {
        return TASKS.remove(id);
    }

    public CallbackTask assign(String id, CallbackTask task) {
        return TASKS.put(id, task);
    }

}
