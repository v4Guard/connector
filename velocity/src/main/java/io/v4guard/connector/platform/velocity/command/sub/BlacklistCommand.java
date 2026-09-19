package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.commands.AnnotatedCommand;
import io.v4guard.connector.common.request.BlacklistRequest;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotation.specifier.FlagYielding;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Flag;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Permission("v4guard.command.blacklist")
@Command("v4guard|v4g blacklist")
public class BlacklistCommand implements AnnotatedCommand {

    private final BlacklistRequest blacklistRequest;
    private final VelocityInstance plugin;

    private final List<String> defaultHelpMessage = List.of(
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard blacklist add <username> <reason_preset> [reason] [-i] [-p] [-s]</yellow>",
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard blacklist remove <id-blacklist></yellow>"
    );

    public BlacklistCommand(VelocityInstance plugin) {
        this.plugin = plugin;
        this.blacklistRequest = new BlacklistRequest();
    }

    @Command("")
    public void help(CommandSource source) {
        Component help = plugin
                .getComponentAdapter()
                .adapt(CoreInstance.get().getActiveSettings().getMessage("blacklistHelp", defaultHelpMessage));

        if (help == null) return;
        source.sendMessage(help);
    }

    @Command("add <target> <reason_preset> <reason>")
    public void addBlacklist(CommandSource source,
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
                source instanceof Player ? ((Player) source).getUsername() : "Console"
        ).thenAccept(success -> {
            Component message = plugin.getComponentAdapter().adapt(
                    CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistAdd" : "blacklistAddFailed"),
                    Map.of("username", value)
            );

            if (message == null) return;

            source.sendMessage(message);
        });
    }

    @Command("remove <code>")
    public void removeBlacklist(CommandSource source, @Argument("code") String id) {
        blacklistRequest
                .removeBlacklist(id)
                .thenAccept(success -> {
                    Component message = plugin.getComponentAdapter().adapt(
                            CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistRemove" : "blacklistRemoveFailed"),
                            Map.of("id", id)
                    );

                    if (message == null) return;
                    source.sendMessage(message);
                });
    }

    @Suggestions("target")
    public List<String> target(CommandContext<CommandSource> source, String input) {
        return Collections.singletonList("<username/ip>");
    }

    @Suggestions("reason_preset")
    public List<String> reasonPreset(CommandContext<CommandSource> source, String input) {
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

    @Suggestions("reason")
    public List<String> reason(CommandContext<CommandSource> source, String input) {
        return Collections.singletonList("<reason>");
    }

    @Suggestions("code")
    public List<String> code(CommandContext<CommandSource> source, String input) {
        return Collections.singletonList("<code>");
    }


}
