package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.check.CheckStatus;
import org.bson.Document;

public class CleanCacheListener implements Emitter.Listener {

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());

        if(doc.containsKey("username")) {
            CoreInstance.get().getCheckDataCache().cleanup(doc.getString("username"));
        } else {
            CoreInstance.get().getCheckDataCache().invalidateAllThatNot(CheckStatus.WAITING);
        }
    }

}