package io.v4guard.connector.platform.velocity.check;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.ResultedEvent.ComponentResult;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.CheckProcessor;
import io.v4guard.connector.common.check.CheckStatus;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import io.v4guard.connector.platform.velocity.event.PostCheckEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
        Player player = event.getPlayer();
        boolean isFloodgatePlayer = CoreInstance.get().isFloodgateFound()
                && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());

        PlayerCheckData checkData = prepareCheckData(
                username
                , player.getRemoteAddress().getAddress().getHostAddress()
                , player.getProtocolVersion().getProtocol()
                , virtualHost.isPresent() ? virtualHost.get().getHostString() : "notFound"
                , isFloodgatePlayer
        );

        CoreInstance.get().getCheckDataCache().cache(username, checkData);

        if (!checkData.isWaitMode()) {
            continuation.resume();
        }

        checkData.whenCompleted((exception) -> {
            if (exception != null) {
                UnifiedLogger.get().log(
                        Level.SEVERE
                        , "An exception has occurred while checking player '" + username + "'"
                        , exception
                );

                processFinalStage(event, checkData, continuation, false);
                return;
            }

            PostCheckEvent apiEvent = new PostCheckEvent(username, checkData.getBlockReason());

            this.plugin.getServer().getEventManager().fire(apiEvent).thenAccept((resultEvent) -> {
                boolean disconnect = checkData.getCheckStatus() == CheckStatus.USER_DENIED
                        && resultEvent.getResult().isAllowed();

                processFinalStage(event, checkData, continuation, disconnect);
            });


        });

        checkData.startChecking();
    }

    private void processFinalStage(
            LoginEvent event
            , PlayerCheckData checkData
            , Continuation continuation
            , boolean disconnect
    ) {
        if (checkData.isWaitMode() && disconnect) {
            Component kickComponent = this.plugin.getComponentAdapter().adapt(
                    checkData.getKickReason(),
                    event.getPlayer().getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_16.getProtocol()
            );

            event.setResult(ComponentResult.denied(kickComponent != null ? kickComponent : Component.text("An error occurred while processing your login")));
        } else if (disconnect) {
            plugin.kickPlayer(event.getPlayer().getUsername(), checkData.getKickReason(), true);
        }

        if (checkData.isWaitMode()) {
            continuation.resume();
        }
    }

}
