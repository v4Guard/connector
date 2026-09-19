package io.v4guard.connector.platform.bungee.command.sub;

import com.mojang.brigadier.context.CommandContext;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.commands.AnnotatedCommand;
import io.v4guard.connector.common.request.WhitelistRequest;
import io.v4guard.connector.platform.bungee.BungeeInstance;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Permission("v4guard.command.whitelist")
@Command("v4guard whitelist")
public class WhitelistCommand implements AnnotatedCommand {

    private final BungeeInstance plugin;
    private final WhitelistRequest whitelistRequest;

    private final List<String> defaultHelpMessage = List.of(
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard whitelist add <username></yellow>",
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard whitelist remove <username></yellow>"
    );

    public WhitelistCommand(BungeeInstance plugin) {
        this.plugin = plugin;
        this.whitelistRequest = new WhitelistRequest();
    }


    @Command("")
    public void help(CommandSender source) {
        BaseComponent[] help = plugin.getComponentAdapter().adapt(
                CoreInstance.get().getActiveSettings().getMessage("whitelistHelp", defaultHelpMessage)
        );

        if (help == null) return;
        source.sendMessage(help);
    }

    @Command("add <username>")
    public void addWhitelist(CommandSender source, @Argument(value = "username", suggestions = "username") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.addWhitelist(player, source instanceof ProxiedPlayer ? ((ProxiedPlayer) source).getDisplayName() : "Console");

        future.thenAccept(success -> {
            BaseComponent[] message = plugin.getComponentAdapter().adapt(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistAdd" : "whitelistAddFailed"),
                    Map.of("username", player)
            );

            if (message == null) return;
            source.sendMessage(message);
        });
    }

    @Command("remove <username>")
    public void removeWhitelist(CommandSender source, @Argument(value = "username", suggestions = "username") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.removeWhitelist(player);

        future.thenAccept(success -> {
            BaseComponent[] message = plugin.getComponentAdapter().adapt(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistRemove" : "whitelistRemoveFailed"),
                    Map.of("username", player)
            );

            if (message == null) return;
            source.sendMessage(message);
        });
    }


    @Suggestions("username")
    public List<String> player(CommandContext<CommandSender> source, String input) {
        return Collections.singletonList("<username>");
    }

}
