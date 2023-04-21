package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerClientBrandEvent;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.plugin.core.tasks.types.CompletableMCBrandTask;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import org.bson.Document;

public class PlayerBrandListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerServerBrand(PlayerClientBrandEvent e, Continuation continuation) {
        if (e.getBrand().equalsIgnoreCase("labymod")) return;
        
        Player player = e.getPlayer();
        if (v4GuardVelocity.getCoreInstance().getBrandCheckManager().isPlayerAlreadyChecked(player.getUniqueId())) return;

        Document privacySettings = (Document) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
        boolean invalidatedCache = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);

        if (invalidatedCache && !privacySettings.getBoolean("collectMCBrand", true)) return;

        CompletableMCBrandTask task = v4GuardVelocity.getCoreInstance().getCompletableTaskManager().getBrandTask(player.getUsername());
        if(task == null) task = new CompletableMCBrandTask(player.getUsername());

        task.addData(e.getBrand());
        v4GuardVelocity.getCoreInstance().getBrandCheckManager().addPlayer(player.getUniqueId());
        continuation.resume();
    }


    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerQuit(DisconnectEvent e) {
        v4GuardCore.getInstance().getBrandCheckManager().removePlayer(e.getPlayer().getUniqueId());
    }
}
