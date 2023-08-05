package io.v4guard.plugin.core.socket.listener;

import io.socket.emitter.Emitter;
import io.v4guard.plugin.core.UnifiedLogger;
import io.v4guard.plugin.core.constants.ListenersConstants;
import org.bson.Document;

import java.io.IOException;
import java.util.logging.Level;

@Deprecated
public class IPSetListener implements Emitter.Listener {

    private final Runtime RUNTIME = Runtime.getRuntime();
    private final String RULE_NAME = "v4guard";
    private final String IPSET_ADD_COMMAND = "ipset add " + RULE_NAME + " %s timeout %s";
    private final String IPSET_FLUSH_COMMAND = "ipset flush " + RULE_NAME;

    @Override
    public void call(Object... args) {
        Document doc = Document.parse(args[0].toString());

        try {
            String action = doc.getString(ListenersConstants.IPSET_ACTION);

            if (action.equals(ListenersConstants.IPSET_ACTION_ADD)) {
                RUNTIME.exec(String.format(
                        IPSET_ADD_COMMAND
                        , doc.getString(ListenersConstants.IPSET_IP)
                        , doc.getString(ListenersConstants.IPSET_TIMEOUT)
                ));
            } else if (action.equals(ListenersConstants.IPSET_ACTION_FLUSH)) {
                RUNTIME.exec(IPSET_FLUSH_COMMAND);
            }
        } catch (IOException exception) {
            UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred while handling ipset command.", exception);
        }
    }
}
