package io.v4guard.connector.platform.bungee.command.sub;

import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.command.internal.annotations.CommandFlag;
import io.v4guard.connector.common.request.BlacklistRequest;
import io.v4guard.connector.common.utils.StringUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.*;

import java.util.Map;

@Command(names = "blacklist", permission = "v4guard.command.blacklist")
public class BlacklistCommand implements CommandClass {

    private final BlacklistRequest blacklistRequest;

    public BlacklistCommand() {
        this.blacklistRequest = new BlacklistRequest();
    }

    @Command(names = "")
    public void help(@Sender CommandSender source) {
        source.sendMessage(new ComponentBuilder("§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist add <username> <reason_preset> <reason> [-i] [-p] [-s]").create());
        source.sendMessage(new ComponentBuilder("§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist remove <id-blacklist>").create());
    }

    @Command(names = "add")
    public void addBlacklist(@Sender CommandSender source,
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
                source instanceof ProxiedPlayer ? source.getName() : "Console"
        ).thenAccept(success -> {
            String message = StringUtils.buildMultilineString(
                    CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistAdd" : "blacklistAddFailed")
            );
            source.sendMessage(new ComponentBuilder(StringUtils.replacePlaceholders(message, Map.of("username", value))).create());
        });
    }

    @Command(names = "remove")
    public void removeBlacklist(@Sender CommandSender source, @Suggestions(suggestions = "<code>") String id) {
        blacklistRequest.removeBlacklist(id)
                .thenAccept(success -> {
                    String message = StringUtils.buildMultilineString(
                            CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistRemove" : "blacklistRemoveFailed")
                    );
                    source.sendMessage(new ComponentBuilder(StringUtils.replacePlaceholders(message, Map.of("id", id))).create());
                });

    }

}
