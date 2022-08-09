package io.v4guard.plugin.velocity;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.CheckStatus;
import io.v4guard.plugin.core.utils.StringUtils;
import net.kyori.adventure.text.Component;
import org.bson.Document;

import java.util.List;
import java.util.Optional;

public class VelocityCheckProcessor implements CheckProcessor {

    public void onPreLoginWithContinuation(Object event, Continuation continuation) {
        PreLoginEvent e = (PreLoginEvent) event;
        final boolean wait = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        CheckStatus status = v4GuardBungee.getCoreInstance().getCheckManager().getCheckStatus(e.getUsername());
        if (status != null && status.isBlocked()) {
            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(status.getReason())));
            return;
        }
        if (!wait || !v4GuardVelocity.getCoreInstance().getBackendConnector().getSocketStatus().equals(SocketStatus.AUTHENTICATED)) {
            if(continuation != null) continuation.resume();
            return;
        }
        doChecks(e, continuation);
    }

    @Override
    public void onPreLogin(String username, Object event) {
        PreLoginEvent e = (PreLoginEvent) event;
        final boolean wait = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        if(wait || !v4GuardVelocity.getCoreInstance().getBackendConnector().getSocketStatus().equals(SocketStatus.AUTHENTICATED)) return;
        doChecks(e, null);
    }

    @Override
    public void onLogin(String username, Object event) {
        LoginEvent e = (LoginEvent) event;
        CheckStatus status = v4GuardVelocity.getCoreInstance().getCheckManager().getCheckStatus(e.getPlayer().getUsername());
        if(status != null && status.isBlocked()) e.setResult(ResultedEvent.ComponentResult.denied(Component.text(status.getReason())));
    }

    @Override
    public void onPostLogin(String username, Object event) {
        PostLoginEvent e = (PostLoginEvent) event;
        CheckStatus status = v4GuardVelocity.getCoreInstance().getCheckManager().getCheckStatus(e.getPlayer().getUsername());
        if(status != null && status.isBlocked()) e.getPlayer().disconnect(Component.text(status.getReason()));
    }

    @Override
    public boolean actionOnExpire(CheckStatus status) {
        Optional<Player> pp = v4GuardVelocity.getV4Guard().getServer().getPlayer(status.getName());
        if(status.isBlocked() && pp.isPresent()) {
            pp.get().disconnect(Component.text(status.getReason()));
            return true;
        }
        return false;
    }

    private void doChecks(PreLoginEvent e, Continuation continuation) {
        new CompletableNameCheckTask(e.getUsername()) {
            @Override
            public void complete(boolean nameIsValid) {
                if(nameIsValid){
                    new CompletableIPCheckTask(e.getConnection().getRemoteAddress().getAddress().getHostAddress(), e.getUsername(), e.getConnection().getProtocolVersion().getProtocol()){
                        @Override
                        public void complete() {
                            CheckStatus check = this.getCheck();
                            if (check.isBlocked()) {
                                if(actionOnExpire(check)) return;
                                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(check.getReason())));
                            }
                            if(continuation != null) continuation.resume();
                        }
                    };
                } else {
                    String username = this.getUsername();
                    Document kickMessages = (Document) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("messages");
                    String kickReasonMessage = StringUtils.buildMultilineString((List<String>) kickMessages.get("invalidUsername"));
                    kickReasonMessage = StringUtils.replacePlaceholders(kickReasonMessage, new Document("username", username));
                    e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(kickReasonMessage)));
                    if(continuation != null) continuation.resume();
                }
            }
        };
    }
}
