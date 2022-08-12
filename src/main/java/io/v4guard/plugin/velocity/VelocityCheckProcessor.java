package io.v4guard.plugin.velocity;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.check.common.CheckStatus;
import io.v4guard.plugin.core.check.common.VPNCheck;
import io.v4guard.plugin.core.tasks.types.CompletableIPCheckTask;
import io.v4guard.plugin.core.tasks.types.CompletableNameCheckTask;
import io.v4guard.plugin.core.utils.StringUtils;
import net.kyori.adventure.text.Component;
import org.bson.Document;

import java.util.List;
import java.util.Optional;

public class VelocityCheckProcessor implements CheckProcessor {

    public void onPreLoginWithContinuation(Object event, Continuation continuation) {
        PreLoginEvent e = (PreLoginEvent) event;

        //Clear other checks if player changes his address
        String address = e.getConnection().getRemoteAddress().getAddress().getHostAddress();
        v4GuardVelocity.getCoreInstance().getCheckManager().cleanupChecks(e.getUsername());

        final boolean wait = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        if (!wait) {
            if(continuation != null) continuation.resume();
            return;
        }
        doChecks(e, continuation);
    }

    @Override
    public void onPreLogin(String username, Object event) {
        PreLoginEvent e = (PreLoginEvent) event;

        //Clear other checks if player changes his address
        String address = e.getConnection().getRemoteAddress().getAddress().getHostAddress();
        v4GuardVelocity.getCoreInstance().getCheckManager().cleanupChecks(e.getUsername());

        final boolean wait = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().get("waitResponse");
        if(wait) return;
        doChecks(e, null);
    }

    @Override
    public void onPostLogin(String username, Object event) {
        VPNCheck check = v4GuardVelocity.getCoreInstance().getCheckManager().getCheckStatus(username);
        if(check == null) return;
        check.setPostLoginTime(System.currentTimeMillis());
        if(check.getStatus() == CheckStatus.USER_DENIED){
            PostLoginEvent e = (PostLoginEvent) event;
            Player player = e.getPlayer();
            player.disconnect(Component.text(check.getReason()));
        }
    }

    @Override
    public boolean onExpire(VPNCheck status) {
        Optional<Player> pp = v4GuardVelocity.getV4Guard().getServer().getPlayer(status.getName());
        if(status.getStatus() == CheckStatus.USER_DENIED && pp.isPresent()) {
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
                            VPNCheck check = this.getCheck();
                            if (check.getStatus() == CheckStatus.USER_DENIED) {
                                if(onExpire(check)) return;
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
