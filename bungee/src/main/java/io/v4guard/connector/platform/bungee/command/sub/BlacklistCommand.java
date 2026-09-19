package io.v4guard.connector.platform.bungee.command.sub;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.commands.AnnotatedCommand;
import io.v4guard.connector.common.request.BlacklistRequest;
import io.v4guard.connector.platform.bungee.BungeeInstance;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.incendo.cloud.annotation.specifier.FlagYielding;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.*;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Permission("v4guard.command.blacklist")
@Command("v4guard blacklist")
public class BlacklistCommand implements AnnotatedCommand {

    private final BlacklistRequest blacklistRequest;
    private final BungeeInstance plugin;

    private final List<String> defaultHelpMessage = List.of(
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard blacklist add <username> <reason_preset> [reason] [-i] [-p] [-s]</yellow>",
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard blacklist remove <id-blacklist></yellow>"
    );

    public BlacklistCommand(BungeeInstance plugin) {
        this.plugin = plugin;
        this.blacklistRequest = new BlacklistRequest();
    }

    @Command("")
    public void help(CommandSender source) {
        BaseComponent[] help = plugin
                .getComponentAdapter()
                .adapt(CoreInstance.get().getActiveSettings().getMessage("blacklistHelp", defaultHelpMessage));

        if (help == null) return;
        source.sendMessage(help);
    }

    @Command("add <target> <reason_preset> <reason>")
    public void addBlacklist(CommandSender source,
                             @Argument(value = "target", suggestions = "target") String value,
                             @Argument(value = "reason_preset", suggestions = "reason_preset") String preset,
                             @Flag("i") boolean ipBan,
                             @Flag("s") boolean silent,
                             @Flag("p") boolean propagate,
                             @Argument(value = "reason", suggestions = "reason") @FlagYielding String reason
    ) {
        blacklistRequest.addBlacklist(
                value,
                preset,
                reason,
                ipBan,
                silent,
                propagate,
                source instanceof ProxiedPlayer ? source.getName() : "Console"
        ).thenAccept(success -> {
            BaseComponent[] message = plugin.getComponentAdapter().adapt(
                    CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistAdd" : "blacklistAddFailed"),
                    Map.of("username", value)
            );

            if (message == null) return;

            source.sendMessage(message);
        });
    }

    @Command("remove <code>")
    public void removeBlacklist(CommandSender source, @Argument(value = "code", suggestions = "code") String id) {
        blacklistRequest
                .removeBlacklist(id)
                .thenAccept(success -> {
                    BaseComponent[] message = plugin.getComponentAdapter().adapt(
                            CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistRemove" : "blacklistRemoveFailed"),
                            Map.of("id", id)
                    );

                    if (message == null) return;
                    source.sendMessage(message);
                });

    }

    @Suggestions("target")
    public List<String> target(CommandContext<CommandSender> source, String input) {
        return Collections.singletonList("<username/ip>");
    }

    @Suggestions("reason")
    public List<String> reason(CommandContext<CommandSender> source, String input) {
        return Collections.singletonList("<reason>");
    }

    @Suggestions("code")
    public List<String> code(CommandContext<CommandSender> source, String input) {
        return Collections.singletonList("<code>");
    }


    @Suggestions("reason_preset")
    public List<String> reasonPreset(CommandContext<CommandSender> source, String input) {
        return List.of(
                "cheating_or_illegal_modifications",
                "server_griefing",
                "botting",
                "account_stealing",
                "server_crashing",
                "server_exploiting",
                "ban_evading",
                "duping",
                "other"
        );
    }
}
