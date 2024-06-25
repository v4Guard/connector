package io.v4guard.connector.platform.velocity.check;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.connection.LoginEvent;;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.check.CheckProcessor;
import io.v4guard.connector.common.check.CheckStatus;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import io.v4guard.connector.platform.velocity.event.PostCheckEvent;
import net.kyori.adventure.text.Component;
import org.geysermc.floodgate.api.FloodgateApi;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.logging.Level;

public class VelocityCheckProcessor extends CheckProcessor<LoginEvent> {

    private final VelocityInstance plugin;

    public VelocityCheckProcessor(VelocityInstance plugin) {
        this.plugin = plugin;
    }

    public void onEvent(String username, LoginEvent event, Continuation continuation) {
        Optional<InetSocketAddress> virtualHost = event.getPlayer().getVirtualHost();

        PlayerCheckData checkData = prepareCheckData(
                username
                , event.getPlayer().getRemoteAddress().getAddress().getHostAddress()
                , event.getPlayer().getProtocolVersion().getProtocol()
                , virtualHost.isPresent() ? virtualHost.get().getHostString() : "notFound"
                , plugin.isFloodgatePresent() && FloodgateApi.getInstance().isFloodgatePlayer(event.getPlayer().getUniqueId())
        );

        CoreInstance.get().getCheckDataCache().cache(username, checkData);

        if (!checkData.isWaitMode()) {
            continuation.resume();
        }

        checkData.whenCompleted((exception) -> {
            if (exception != null) {
                plugin.getLogger().log(Level.SEVERE, "An exception has occurred while checking player '" + username + "'", exception);
            } else {
                PostCheckEvent apiEvent = new PostCheckEvent(username, checkData.getBlockReason());

                this.plugin.getServer().getEventManager().fire(apiEvent).thenAccept((resultEvent) -> {
                    if (checkData.getCheckStatus() == CheckStatus.USER_DENIED && resultEvent.getResult().isAllowed()) {
                        if (checkData.isWaitMode()) {
                            event.setResult(ComponentResult.denied(Component.text(checkData.getKickReason())));
                        } else {
                            event.getPlayer().disconnect(Component.text(checkData.getKickReason()));
                        }
                    }
                });
            }

            if (checkData.isWaitMode()) {
                continuation.resume();
            }
        });

        checkData.startChecking();
    }

}
