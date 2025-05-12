package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import io.v4guard.connector.common.UnifiedLogger;
import io.v4guard.connector.common.request.WhitelistRequest;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Command(names = "whitelist")
public class WhitelistCommand implements CommandClass {
    private final WhitelistRequest whitelistRequest;

    public WhitelistCommand() {
        this.whitelistRequest = new WhitelistRequest();
    }


    @Command(names = "")
    public void help(@Sender CommandSource source) {
        source.sendPlainMessage("correct syntax: /connector whitelist <add/remove> <player>");

    }

    @Command(names = "add")
    public void addWhitelist(@Sender CommandSource source, String player) {

        CompletableFuture<Boolean> future = whitelistRequest.addWhitelist(player);

        future.whenComplete((success, ex) -> {
            if (ex != null) {
                UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred on the response to add whitelist ", ex);
            }
            if(success) {
                source.sendPlainMessage(player + " has been added to the whitelist");
            } else {
                source.sendPlainMessage(player + " couldn't be added to the whitelist");
            }

        });


    }


    @Command(names = "remove")
    public void removeWhitelist(@Sender CommandSource source, String player) {
        CompletableFuture<Boolean> future = whitelistRequest.removeWhitelist(player);

        future.whenComplete((success, ex) -> {
            if (ex != null) {
                UnifiedLogger.get().log(Level.SEVERE, "An exception has occurred on the response to add whitelist ", ex);
            }
            if(success) {
                source.sendPlainMessage(player + " has been removed of the whitelist");
            } else {
                source.sendPlainMessage(player + " couldn't be removed of the whitelist");
            }

        });


    }

}
