package io.v4guard.connector.platform.bungee.command.sub;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.request.WhitelistRequest;
import io.v4guard.connector.common.utils.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
    public void help(@Sender CommandSender source) {

        source.sendMessage(new ComponentBuilder("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist add <username>").create());
        source.sendMessage(new ComponentBuilder("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist remove <username>").create());

    }

    @Command(names = "add")
    public void addWhitelist(@Sender CommandSender source, @Suggestions(suggestions = "<username>") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.addWhitelist(player, source instanceof ProxiedPlayer ? source.getName() : null);

        future.thenAccept(success -> {
            String message = StringUtils.buildMultilineString(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistAdd" : "whitelistAddFailed")
            );

            source.sendMessage(new ComponentBuilder(StringUtils.replacePlaceholders(message, Map.of("username", player))).create());

        });

    }


    @Command(names = "remove")
    public void removeWhitelist(@Sender CommandSender source, @Suggestions(suggestions = "<username>") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.removeWhitelist(player);

        future.thenAccept(success -> {
            String message = StringUtils.buildMultilineString(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistRemove" : "whitelistRemoveFailed")
            );

            source.sendMessage(new ComponentBuilder(StringUtils.replacePlaceholders(message, Map.of("username", player))).create());

        });


    }

}
