package io.v4guard.connector.platform.velocity.command.sub;

import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.commands.AnnotatedCommand;
import io.v4guard.connector.common.request.WhitelistRequest;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Permission("v4guard.command.whitelist")
@Command("v4guard|v4g whitelist")
public class WhitelistCommand implements AnnotatedCommand {

    private final VelocityInstance plugin;
    private final WhitelistRequest whitelistRequest;

    private final List<String> defaultHelpMessage = List.of(
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard whitelist add <username></yellow>",
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard whitelist remove <username></yellow>"
    );

    public WhitelistCommand(VelocityInstance plugin) {
        this.plugin = plugin;
        this.whitelistRequest = new WhitelistRequest();
    }


    @Command("")
    public void help(CommandSource source) {
        Component help = plugin.getComponentAdapter().adapt(
                CoreInstance.get().getActiveSettings().getMessage("whitelistHelp", defaultHelpMessage)
        );

        if (help == null) return;
        source.sendMessage(help);
    }

    @Command("add <username>")
    public void addWhitelist(CommandSource source,  @Argument(value = "username", suggestions = "username") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.addWhitelist(player, source instanceof Player ? ((Player) source).getUsername() : "Console");

        future.thenAccept(success -> {
            Component message = plugin.getComponentAdapter().adapt(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistAdd" : "whitelistAddFailed")
            );

            if (message == null) return;
            source.sendMessage(message);
        });

    }


    @Command("remove <username>")
    public void removeWhitelist(CommandSource source, @Argument(value = "username", suggestions = "username") String player) {
        CompletableFuture<Boolean> future = whitelistRequest.removeWhitelist(player);

        future.thenAccept(success -> {
            Component message = plugin.getComponentAdapter().adapt(
                    CoreInstance.get().getActiveSettings().getMessage(success  ?  "whitelistRemove" : "whitelistRemoveFailed")
            );

            if (message == null) return;
            source.sendMessage(message);
        });
    }


    @Suggestions("username")
    public List<String> player(CommandContext<CommandSource> source, String input) {
        return Collections.singletonList("<username>");
    }

}
