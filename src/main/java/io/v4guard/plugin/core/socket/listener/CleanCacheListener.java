package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.check.common.CheckStatus;
import io.v4guard.plugin.core.check.common.VPNCheck;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.v4GuardCore;
import org.bson.Document;

public class CleanCacheListener implements Emitter.Listener {

    BackendConnector backendConnector;

    public CleanCacheListener(BackendConnector backendConnector) {
        this.backendConnector = backendConnector;
    }

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());
        if(doc.containsKey("username")){
            v4GuardCore.getInstance().getCheckManager().cleanupChecks(doc.getString("username"));
        } else {
            for(VPNCheck s : v4GuardCore.getInstance().getCheckManager().getCheckStatusMap().values()){
                if(s.getStatus() != CheckStatus.WAITING){
                    v4GuardCore.getInstance().getCheckManager().getCheckStatusMap().remove(s.getName());
                }
            }
        }
    }

}
