package io.v4guard.connector.platform.velocity.command.sub;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.request.BlacklistRequest;
import io.v4guard.connector.common.utils.IpAddressUtils;
import io.v4guard.connector.common.utils.StringUtils;
import io.v4guard.connector.platform.velocity.command.internal.annotations.CommandFlag;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.Text;

import java.util.Map;

@Command(names = "blacklist", permission = "v4guard.command.blacklist")
public class BlacklistCommand implements CommandClass {

    private final BlacklistRequest blacklistRequest;

    public BlacklistCommand() {
        this.blacklistRequest = new BlacklistRequest();
    }

    @Command(names = "")
    public void help(@Sender CommandSource source) {
        source.sendPlainMessage("correct syntax: /connector blacklist add <username> <reason_preset> <reason> -ipBan -propagate -s");
        source.sendPlainMessage("correct syntax: /connector blacklist remove <id-blacklist>");

    }

    @Command(names = "add")
    public void addBlacklist(@Sender CommandSource source,
                             @CommandFlag(value = "i", allowFullName = true, hasDefaultValue = true) Boolean ipBan,
                             @CommandFlag(value = "s", allowFullName = true, hasDefaultValue = true) Boolean silent,
                             @CommandFlag(value = "p", allowFullName = true, hasDefaultValue = true) Boolean propagate,
                             String value,
                             String preset,
                             @Text String reason
    ) {
        Map.Entry<String, String> entry;

        if (IpAddressUtils.isValidIPAddress(value)) {
            entry = Map.entry("address", value);
        } else {
            entry = Map.entry("username", value);
        }
        blacklistRequest.addBlacklist(entry, preset, reason, ipBan, silent, propagate, source instanceof Player ? ((Player) source).getUsername() : null)
                .thenAccept(success -> {
                    String message = StringUtils.buildMultilineString(
                            CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistAdd" : "blacklistAddFailed")
                    );
                    source.sendPlainMessage(StringUtils.replacePlaceholders(message, Map.of("username", value)));
                });
    }

    @Command(names = "remove")
    public void removeBlacklist(@Sender CommandSource source, String id) {
        blacklistRequest.removeBlacklist(id)
                .thenAccept(success -> {
                    String message = StringUtils.buildMultilineString(
                            CoreInstance.get().getActiveSettings().getMessage(success ? "blacklistRemove" : "blacklistRemoveFailed")
                    );
                    source.sendPlainMessage(StringUtils.replacePlaceholders(message, Map.of("id", id)));
                });

    }

}
