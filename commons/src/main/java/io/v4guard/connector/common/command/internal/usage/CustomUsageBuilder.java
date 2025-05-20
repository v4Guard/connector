package io.v4guard.connector.common.command.internal.usage;

import net.kyori.adventure.text.Component;
import team.unnamed.commandflow.CommandContext;
import team.unnamed.commandflow.usage.UsageBuilder;

public class CustomUsageBuilder implements UsageBuilder {

    @Override
    public Component getUsage(CommandContext commandContext) {
        switch (commandContext.getArguments().get(1)) {
            case "blacklist":
                switch (commandContext.getCommand().getName()) {
                    case "add":
                        return Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist add <username> <reason_preset> <reason> [-i] [-p] [-s]");
                    case "remove":
                        return Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist remove <id-blacklist>");
                }
            case "whitelist":
                switch (commandContext.getCommand().getName()) {
                    case "add":
                        return Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist add <username>");
                    case "remove":
                        return Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist remove <username>");
                }
            default:
                return Component.text("§d▲ §lV4GUARD §7Unrecognized command. Please use /v4guard <whitelist>/<blacklist>");
        }
    }
}
