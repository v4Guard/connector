package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.request.WhitelistRequest;
import io.v4guard.connector.common.utils.StringUtils;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.Suggestions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Command(names = "whitelist", permission = "v4guard.command.whitelist")
public class WhitelistCommand implements CommandClass {

    private final VelocityInstance plugin;
    private final WhitelistRequest whitelistRequest;

    private final List<String> defaultHelpMessage = List.of(
            "§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist add <username>",
            "§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist remove <username>"
    );

    public WhitelistCommand(VelocityInstance plugin) {
        this.plugin = plugin;
        this.whitelistRequest = new WhitelistRequest();
    }


    @Command(names = "")
    public void help(@Sender CommandSource source) {
        List<String> help = CoreInstance.get().getActiveSettings().getMessage("whitelistHelp", defaultHelpMessage);

        help.forEach(line -> source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(line)));
    }

    @Command(names = "add")
    public void addWhitelist(@Sender CommandSource source, @Suggestions(suggestions = "<username>") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.addWhitelist(player, source instanceof Player ? ((Player) source).getUsername() : null);

        future.thenAccept(success -> {
            String message = StringUtils.buildMultilineString(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistAdd" : "whitelistAddFailed")
            );

            source.sendMessage(
                    plugin.getLegacyComponentSerializer().deserialize(StringUtils.replacePlaceholders(message, Map.of("username", player)))
            );
        });

    }


    @Command(names = "remove")
    public void removeWhitelist(@Sender CommandSource source, @Suggestions(suggestions = "<username>") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.removeWhitelist(player);

        future.thenAccept(success -> {
            String message = StringUtils.buildMultilineString(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistRemove" : "whitelistRemoveFailed")
            );

            source.sendMessage(
                    plugin.getLegacyComponentSerializer().deserialize(StringUtils.replacePlaceholders(message, Map.of("username", player)))
            );
        });


    }

}
