package io.v4guard.plugin.bungee.check;

import io.v4guard.plugin.bungee.BungeeInstance;
import io.v4guard.plugin.bungee.cache.BungeeCheckDataCache;
import io.v4guard.plugin.bungee.event.PostCheckEvent;
import io.v4guard.plugin.core.CoreInstance;
import io.v4guard.plugin.core.UnifiedLogger;
import io.v4guard.plugin.core.check.CheckProcessor;
import io.v4guard.plugin.core.check.CheckStatus;
import io.v4guard.plugin.core.check.PlayerCheckData;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;

import java.util.logging.Level;

public class BungeeCheckProcessor extends CheckProcessor<LoginEvent> {

    private BungeeInstance plugin;

    public BungeeCheckProcessor(BungeeInstance plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEvent(String username, LoginEvent event) {
        PlayerCheckData checkData = prepareCheckData(
                username
                , event.getConnection().getSocketAddress().toString()
                , event.getConnection().getVersion()
                , event.getConnection().getVirtualHost().getHostString()
        );

        plugin.getCheckDataCache().rememberLogin(username, checkData);

        if (checkData.isWaitMode()) {
            event.registerIntent(plugin);
        }

        checkData.whenCompleted((exception) -> {
            if (exception != null) {
                plugin.getLogger().log(Level.SEVERE, "An exception has occurred while checking player '" + username + "'", exception);
            } else {
                PostCheckEvent apiEvent = new PostCheckEvent(username, checkData.getBlockReason());

                this.plugin.getProxy().getPluginManager().callEvent(apiEvent);

                if (checkData.getCheckStatus() == CheckStatus.USER_DENIED && !apiEvent.isCancelled()) {
                    if (checkData.isWaitMode()) {
                        event.setCancelled(true);
                        event.setCancelReason(TextComponent.fromLegacyText(checkData.getKickReason()));
                    } else {
                        event.getConnection().disconnect(TextComponent.fromLegacyText(checkData.getKickReason()));
                    }
                }
            }

            if (checkData.isWaitMode()) {
                event.completeIntent(plugin);
            }
        });

        checkData.startChecking();

    }
}
