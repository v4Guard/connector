package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableMCBrandTask;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import org.bson.Document;

public class PluginMessagingListener {

    @Subscribe
    public void onMessage(PluginMessageEvent e) {
        if (v4GuardVelocity.getCoreInstance().getBackendConnector().getSocketStatus() != SocketStatus.AUTHENTICATED) return;
        if (!(e.getSource() instanceof Player)) return;

        Player player = (Player) e.getSource();
        if (v4GuardVelocity.getCoreInstance().getBrandCheckManager().isPlayerAlreadyChecked(player.getUniqueId())) return;

        if (e.getIdentifier().getId().equals("LMC") || e.getIdentifier().getId().equals("labymod3:main")) {
            Document privacySettings = (Document) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
            boolean invalidatedCache = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);

            if (invalidatedCache && !privacySettings.getBoolean("collectMCBrand", true)) return;

            CompletableMCBrandTask task = v4GuardVelocity.getCoreInstance().getCompletableTaskManager().getBrandTask(player.getUsername());
            if(task == null) task = new CompletableMCBrandTask(player.getUsername());

            ByteBuf buf = Unpooled.wrappedBuffer(e.getData());
            String key = StringUtils.readString(buf, Short.MAX_VALUE);
            if(!key.equals("INFO")){
                return;
            }

            String json = StringUtils.readString(buf, Short.MAX_VALUE);
            Document data = Document.parse(json);
            String version = data == null ? "unknown" : (String) data.getOrDefault("version", "unknown");
            task.addData("labymod:" + version);
            v4GuardVelocity.getCoreInstance().getBrandCheckManager().addPlayer(player.getUniqueId());
        }
    }
}
