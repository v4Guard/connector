package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.request.BlacklistRequest;
import io.v4guard.connector.common.utils.StringUtils;
import io.v4guard.connector.common.command.internal.annotations.CommandFlag;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.*;

import java.util.List;
import java.util.Map;

@Command(names = "blacklist", permission = "v4guard.command.blacklist")
public class BlacklistCommand implements CommandClass {

    private final BlacklistRequest blacklistRequest;
    private final VelocityInstance plugin;

    private final List<String> defaultHelpMessage = List.of(
            "§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist add <username> <reason_preset> <reason> [-i] [-p] [-s]",
            "§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist remove <id-blacklist>"
    );

    public BlacklistCommand(VelocityInstance plugin) {
        this.plugin = plugin;
        this.blacklistRequest = new BlacklistRequest();
    }

    @Command(names = "")
    public void help(@Sender CommandSource source) {
        List<String> help = CoreInstance.get().getActiveSettings().getMessage("blacklistHelp", defaultHelpMessage);

        help.forEach(line -> source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(line)));
    }

    @Command(names = "add")
    public void addBlacklist(@Sender CommandSource source,
                             @Suggestions(suggestions = {"<username>", "<ip>"}) String value,
                             @Suggestions(suggestions =
                                     {
                                             "cheating_or_illegal_modifications",
                                             "server_griefing",
                                             "botting",
                                             "account_stealing",
                                             "server_crashing",
                                             "server_exploting",
                                             "ban_evading",
                                             "duping",
                                             "other"
                                     }) String preset,
                             @CommandFlag(value = "i", allowFullName = true, hasDefaultValue = true) Boolean ipBan,
                             @CommandFlag(value = "s", allowFullName = true, hasDefaultValue = true) Boolean silent,
                             @CommandFlag(value = "p", allowFullName = true, hasDefaultValue = true) Boolean propagate,
                             @OptArg("") @Text String reason
    ) {
        blacklistRequest.addBlacklist(
                        value,
                        preset,
                        reason,
                        ipBan,
                        silent,
                        propagate,
                        source instanceof Player ? ((Player) source).getUsername() : null
                ).thenAccept(success -> {
                    String message = StringUtils.buildMultilineString(
                            CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistAdd" : "blacklistAddFailed")
                    );

                    source.sendMessage(
                            plugin.getLegacyComponentSerializer().deserialize(StringUtils.replacePlaceholders(message, Map.of("username", value)))
                    );
                });
    }

    @Command(names = "remove")
    public void removeBlacklist(@Sender CommandSource source, @Suggestions(suggestions = "<code>") String id) {
        blacklistRequest
                .removeBlacklist(id)
                .thenAccept(success -> {
                    String message = StringUtils.buildMultilineString(
                            CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistRemove" : "blacklistRemoveFailed")
                    );

                    source.sendMessage(
                            plugin.getLegacyComponentSerializer().deserialize(StringUtils.replacePlaceholders(message, Map.of("id", id)))
                    );
                });

    }

}
