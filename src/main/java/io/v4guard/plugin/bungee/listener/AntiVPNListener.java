package io.v4guard.plugin.bungee.listener;

import io.v4guard.plugin.bungee.BungeeInstance;
import io.v4guard.plugin.core.CoreInstance;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class AntiVPNListener implements Listener {

    private BungeeInstance plugin;

    public AntiVPNListener(BungeeInstance plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(LoginEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getConnection() == null) {
            return;
        }

        if (!CoreInstance.get().getBackend().isReady()) {
            return;
        }

        plugin.getCheckProcessor().onEvent(event.getConnection().getName(), event);
    }

}
