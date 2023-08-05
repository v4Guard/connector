package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.velocity.check.VelocityCheckProcessor;

public class AntiVPNListener {

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncLogin(LoginEvent event, Continuation continuation) {
        if(!event.getResult().isAllowed()) {
            return;
        }

        if (CoreInstance.get().getBackend().isReady()) {
            VelocityCheckProcessor checkProcessor = (VelocityCheckProcessor) CoreInstance.get().getPlugin().getCheckProcessor();
            checkProcessor.onEvent(event.getPlayer().getUsername(), event, continuation);
        } else {
            continuation.resume();
        }
    }

}
