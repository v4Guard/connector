package io.v4guard.connector.platform.bungee.check;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.check.CheckProcessor;
import io.v4guard.connector.common.check.CheckStatus;
import io.v4guard.connector.common.check.PlayerCheckData;
import io.v4guard.connector.platform.bungee.BungeeInstance;
import io.v4guard.connector.platform.bungee.event.PostCheckEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.logging.Level;

public class BungeeCheckProcessor extends CheckProcessor<LoginEvent> {

    private final BungeeInstance plugin;

    public BungeeCheckProcessor(BungeeInstance plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEvent(String username, LoginEvent event) {
        PendingConnection connection = event.getConnection();
        boolean isFloodgatePlayer = CoreInstance.get().isFloodgateFound()
                && FloodgateApi.getInstance().isFloodgatePlayer(connection.getUniqueId());

        PlayerCheckData checkData = prepareCheckData(
                username
                , connection.getSocketAddress().toString()
                , connection.getVersion()
                , connection.getVirtualHost().getHostString()
                , isFloodgatePlayer
        );

        plugin.getCheckDataCache().rememberLogin(username, checkData);

        if (checkData.isWaitMode()) {
            event.registerIntent(plugin);
        }

        checkData.whenCompleted((exception) -> {
            if (exception != null) {
                UnifiedLogger.get().log(
                        Level.SEVERE
                        , "An exception has occurred while checking player '" + username + "'"
                        , exception
                );

                processFinalStage(event, checkData, false);
                return;
            }

            PostCheckEvent apiEvent = new PostCheckEvent(username, checkData.getBlockReason());

            this.plugin.getProxy().getPluginManager().callEvent(apiEvent);
            boolean disconnect = checkData.getCheckStatus() == CheckStatus.USER_DENIED
                    && !apiEvent.isCancelled();

            processFinalStage(event, checkData, disconnect);
        });

        checkData.startChecking();
    }

    private void processFinalStage(
            LoginEvent event
            , PlayerCheckData checkData
            , boolean disconnect
    ) {

        if (checkData.isWaitMode() && disconnect) {
            event.setCancelled(true);
            event.setReason(TextComponent.fromLegacy(checkData.getKickReason()));
        } else if (disconnect) {
            plugin.kickPlayer(
                    event.getConnection().getName(),
                    checkData.getKickReason(),
                    true
            );
        }

        if (checkData.isWaitMode()) {
            event.completeIntent(plugin);
        }
    }
}
