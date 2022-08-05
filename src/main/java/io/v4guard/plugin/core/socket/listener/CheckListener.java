package io.v4guard.plugin.core.socket.listener;

import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.socket.emitter.Emitter;
import org.bson.Document;

public class CheckListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        CompletableIPCheckTask task = (CompletableIPCheckTask) v4GuardCore.getInstance().getCompletableTaskManager().getTasks().get(doc.getString("taskID"));
        if (task == null) {
            return;
        }
        task.addData(doc);
        task.check();
    }
}
