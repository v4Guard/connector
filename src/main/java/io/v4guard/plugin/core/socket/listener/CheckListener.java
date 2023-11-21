package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.vpn.VPNCallbackTask;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

public class CheckListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        VPNCallbackTask task = (VPNCallbackTask) CoreInstance.get().getPendingTasks().get(doc.getString("taskID"));

        if (task != null) {
            task.setData(doc);
            task.complete();
        }
    }
}