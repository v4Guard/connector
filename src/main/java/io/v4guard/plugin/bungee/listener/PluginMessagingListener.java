package io.v4guard.plugin.bungee.listener;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.socket.SocketStatus;
import io.v4guard.plugin.core.tasks.types.CompletableMCBrandTask;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.core.v4GuardCore;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PluginMessagingListener implements Listener {


    private final Cache<UUID, Long> TEMP_CACHE = Caffeine
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();


    public PluginMessagingListener() {
        v4GuardBungee.getV4Guard().getProxy().getPluginManager().registerListener(v4GuardBungee.getV4Guard(), this);
    }

    @EventHandler
    public void onChannelMessage(PluginMessageEvent e) {
        if (v4GuardBungee.getCoreInstance().getBackendConnector().getSocketStatus() != SocketStatus.AUTHENTICATED) return;
        if (!(e.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) e.getSender();
        if (v4GuardBungee.getCoreInstance().getBrandCheckManager().isPlayerAlreadyChecked(player.getUniqueId()) && !TEMP_CACHE.asMap().containsKey(player.getUniqueId())) return;

        if (e.getTag().equals("MC|Brand") || e.getTag().equals("minecraft:brand") || e.getTag().equals("LMC") || e.getTag().equals("labymod3:main")) {
            Document privacySettings = (Document) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
            boolean invalidatedCache = (boolean) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);

            if (invalidatedCache && !privacySettings.getBoolean("collectMCBrand", true)) return;

            CompletableMCBrandTask task = v4GuardBungee.getCoreInstance().getCompletableTaskManager().getBrandTask(player.getName());
            if (task == null) task = new CompletableMCBrandTask(player.getName());

            if (e.getTag().equals("LMC") || e.getTag().equals("labymod3:main")) {
                ByteBuf buf = Unpooled.wrappedBuffer(e.getData());
                String key = StringUtils.readString(buf, Short.MAX_VALUE);
                if (!key.equals("INFO")) {
                    return;
                }
                String json = StringUtils.readString(buf, Short.MAX_VALUE);
                Document data = Document.parse(json);
                String version = data == null ? "unknown" : (String) data.getOrDefault("version", "unknown");

                task.addData("labymod:" + version);
            } else {
                task.addData(new String(e.getData()));
            }

            TEMP_CACHE.put(player.getUniqueId(), System.currentTimeMillis());
            v4GuardBungee.getCoreInstance().getBrandCheckManager().addPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        v4GuardCore.getInstance().getBrandCheckManager().removePlayer(event.getPlayer().getUniqueId());
    }
}
