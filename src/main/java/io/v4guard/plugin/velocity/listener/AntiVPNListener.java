package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import io.v4guard.plugin.core.kick.KickReason;
import io.v4guard.plugin.core.socket.BackendConnector;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.kyori.adventure.text.Component;
import org.bson.Document;

import java.util.HashMap;
import java.util.List;

public class AntiVPNListener {

    private final HashMap<String, KickReason> kickReasonsMap = new HashMap();
    private BackendConnector conn = v4GuardVelocity.getCoreInstance().getBackendConnector();

    @Subscribe(order = PostOrder.FIRST)
    public void onAsyncPreLogin(PreLoginEvent e, Continuation continuation) {
        final boolean wait = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        if (!wait || !conn.getSocketStatus().equals(SocketStatus.AUTHENTICATED)) {
            if(continuation != null) continuation.resume();
            return;
        }
        Document kickMessages = (Document) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("messages");
        doChecks(e, kickMessages, continuation);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(PreLoginEvent e) {
        final boolean wait = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        if(wait || !conn.getSocketStatus().equals(SocketStatus.AUTHENTICATED)) return;
        Document kickMessages = (Document) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("messages");
        doChecks(e, kickMessages, null);
    }

    private void doChecks(PreLoginEvent e, Document kickMessages, Continuation continuation) {
        new CompletableNameCheckTask(e.getUsername()) {
            @Override
            public void complete(boolean nameIsValid) {
                if(nameIsValid){
                    new CompletableIPCheckTask(e.getConnection().getRemoteAddress().getAddress().getHostAddress(), e.getUsername(), e.getConnection().getProtocolVersion().getProtocol()){
                        @Override
                        public void complete() {
                            if (this.isBlocked()) {
                                String username = this.getUsername();
                                String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("kick"));
                                Document data = (Document) this.getData().get("result");
                                kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, (Document) data.get("variables"));
                                KickReason reason = new KickReason(username, kickReasonMessage);
                                if(v4GuardVelocity.getV4Guard().getServer().getPlayer(username) != null) {
                                    kickReasonsMap.put(username, reason);
                                } else {
                                    e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(kickReasonMessage)));
                                }
                            }
                            if(continuation != null) continuation.resume();
                        }
                    };
                } else {
                    String username = this.getUsername();
                    String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("invalidUsername"));
                    kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, new Document("username", username));
                    e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(kickReasonMessage)));
                    if(continuation != null) continuation.resume();
                }
            }
        };
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onLogin(PostLoginEvent e) {
        if (this.kickReasonsMap.containsKey(e.getPlayer().getUsername())) {
            KickReason reason = this.kickReasonsMap.get(e.getPlayer().getUsername());
            if(reason == null) return;
            e.getPlayer().disconnect(Component.text(reason.getMessage()));
            this.kickReasonsMap.remove(e.getPlayer().getUsername());
        }
    }
}
