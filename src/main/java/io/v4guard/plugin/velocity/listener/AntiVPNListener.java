package io.v4guard.plugin.velocity.listener;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import io.v4guard.plugin.velocity.VelocityCheckProcessor;
import io.v4guard.plugin.velocity.v4GuardVelocity;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;

public class AntiVPNListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onAsyncPreLogin(PreLoginEvent e, Continuation continuation) {
        VelocityCheckProcessor pr = (VelocityCheckProcessor) v4GuardVelocity.getCoreInstance().getCheckManager().getProcessorByClass(VelocityCheckProcessor.class);
        pr.onPreLoginWithContinuation(e, continuation);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPreLogin(PreLoginEvent e) {
        v4GuardVelocity.getCoreInstance().getCheckManager().runPreLoginCheck(e.getUsername(), e);
    }

    @EventHandler(priority = Byte.MIN_VALUE)
    public void onPostLogin(PostLoginEvent e) {
        v4GuardVelocity.getCoreInstance().getCheckManager().runPostLoginCheck(e.getPlayer().getName(), e);
    }

}
