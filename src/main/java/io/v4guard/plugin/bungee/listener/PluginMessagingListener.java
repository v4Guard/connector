package io.v4guard.plugin.bungee.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.v4guard.plugin.bungee.v4GuardBungee;
import io.v4guard.plugin.core.tasks.types.CompletableMCBrandTask;
import io.v4guard.plugin.core.utils.StringUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

public class PluginMessagingListener implements Listener {

    public PluginMessagingListener() {
        v4GuardBungee.getV4Guard().getProxy().getPluginManager().registerListener(v4GuardBungee.getV4Guard(), this);
    }

    @EventHandler
    public void onChannelMessage(PluginMessageEvent e){

        Document privacySettings = (Document) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("privacy", new Document());
        boolean invalidatedCache = (boolean) v4GuardBungee.getCoreInstance().getBackendConnector().getSettings().getOrDefault("invalidateCache", false);
        if(!invalidatedCache && privacySettings.getBoolean("collectMCBrand", true)){
            if(e.getSender() instanceof ProxiedPlayer){
                if(e.getTag().equals("MC|Brand") || e.getTag().equals("minecraft:brand")){
                    ProxiedPlayer player = (ProxiedPlayer) e.getSender();
                    CompletableMCBrandTask task = v4GuardBungee.getCoreInstance().getCompletableTaskManager().getBrandTask(player.getName());
                    if(task == null) task = new CompletableMCBrandTask(player.getName());
                    task.addData(new String(e.getData()));
                } else if (e.getTag().equals("LMC") || e.getTag().equals("labymod3:main")){
                    ProxiedPlayer player = (ProxiedPlayer) e.getSender();
                    CompletableMCBrandTask task = v4GuardBungee.getCoreInstance().getCompletableTaskManager().getBrandTask(player.getName());
                    if(task == null) task = new CompletableMCBrandTask(player.getName());
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
