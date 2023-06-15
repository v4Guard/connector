package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import io.v4guard.plugin.core.v4GuardCore;
import io.v4guard.plugin.velocity.VelocityCheckProcessor;
import io.v4guard.plugin.velocity.v4GuardVelocity;

public class AntiVPNListener {

    @Subscribe(order = PostOrder.EARLY)
    public void onAsyncPreLogin(PreLoginEvent e, Continuation continuation) {
        if(!e.getResult().isAllowed()) return;
        if(v4GuardVelocity.getV4Guard().getServer().getPlayer(e.getUsername()).isPresent()) return;
        VelocityCheckProcessor pr = (VelocityCheckProcessor) v4GuardCore.getInstance().getCheckManager().getProcessorByClass(VelocityCheckProcessor.class);
        pr.onPreLoginWithContinuation(e, continuation);
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onPreLogin(PreLoginEvent e) {
        if(!e.getResult().isAllowed()) return;
        v4GuardCore.getInstance().getCheckManager().runPreLoginCheck(e.getUsername(), e);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPostLogin(PostLoginEvent e) {
        v4GuardCore.getInstance().getCheckManager().runPostLoginCheck(e.getPlayer().getUsername(), e);
    }

//    @Subscribe(order = PostOrder.FIRST)
//    public void onChat(PlayerChatEvent e) {
//        if(v4GuardCore.getInstance().getChatFilterManager().canLookupMessage(e.getMessage())){
//            Document data = new Document();
//            Player player = e.getPlayer();
//            data.put("username", player.getUsername());
//            data.put("location", v4GuardCore.getInstance().getCheckManager().getProcessors().get(0).getPlayerServer(player.getUsername()));
//            data.put("message", e.getMessage());
//            v4GuardCore.getInstance().getBackendConnector().getSocket().emit("chatfilter:chat", data.toJson());
//        }
//    }

}
