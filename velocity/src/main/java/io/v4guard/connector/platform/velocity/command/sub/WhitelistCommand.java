package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.request.WhitelistRequest;
import io.v4guard.connector.common.utils.StringUtils;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.Suggestions;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Command(names = "whitelist", permission = "v4guard.command.whitelist")
public class WhitelistCommand implements CommandClass {
    private final WhitelistRequest whitelistRequest;

    public WhitelistCommand() {
        this.whitelistRequest = new WhitelistRequest();
    }


    @Command(names = "")
    public void help(@Sender CommandSource source) {
        source.sendPlainMessage("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist add <username>");
        source.sendPlainMessage("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist remove <username>");

    }

    @Command(names = "add")
    public void addWhitelist(@Sender CommandSource source, @Suggestions(suggestions = "<username>") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.addWhitelist(player, source instanceof Player ? ((Player) source).getUsername() : null);

        future.thenAccept(success -> {
            String message = StringUtils.buildMultilineString(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistAdd" : "whitelistAddFailed")
            );

            source.sendPlainMessage(StringUtils.replacePlaceholders(message, Map.of("username", player)));

        });

    }


    @Command(names = "remove")
    public void removeWhitelist(@Sender CommandSource source, @Suggestions(suggestions = "<username>") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.removeWhitelist(player);

        future.thenAccept(success -> {
            String message = StringUtils.buildMultilineString(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistRemove" : "whitelistRemoveFailed")
            );

            source.sendPlainMessage(StringUtils.replacePlaceholders(message, Map.of("username", player)));

        });


    }

}
