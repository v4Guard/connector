package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.tasks.types.CompletableMCBrandTask;
import io.v4guard.plugin.core.utils.StringUtils;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.md_5.bungee.api.plugin.Listener;
import org.bson.Document;

public class PluginMessagingListener implements Listener {

    @Subscribe(order = PostOrder.FIRST)
    public void onMessage(PluginMessageEvent e){
        Document privacySettings = (Document) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
        boolean invalidatedCache = (boolean) v4GuardVelocity.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);
        if(!invalidatedCache && privacySettings.getBoolean("collectMCBrand", true)){
            if(e.getSource() instanceof Player){
                if(e.getIdentifier().getId().equals("MC|Brand") || e.getIdentifier().getId().equals("minecraft:brand")){
                    Player player = (Player) e.getSource();
                    CompletableMCBrandTask task = v4GuardBungee.getCoreInstance().getCompletableTaskManager().getBrandTask(player.getUsername());
                    if(task == null) task = new CompletableMCBrandTask(player.getUsername());
                    task.addData(new String(e.getData()));
                } else if (e.getIdentifier().getId().equals("LMC") || e.getIdentifier().getId().equals("labymod3:main")){
                    Player player = (Player) e.getSource();
                    CompletableMCBrandTask task = v4GuardBungee.getCoreInstance().getCompletableTaskManager().getBrandTask(player.getUsername());
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
                }
            }
        }
    }


}
