package io.v4guard.connector.common.command.internal.usage;

import net.kyori.adventure.text.Component;
import team.unnamed.commandflow.CommandContext;
import team.unnamed.commandflow.usage.UsageBuilder;

public class CustomUsageBuilder implements UsageBuilder {

    @Override
    public Component getUsage(CommandContext commandContext) {
        return switch (commandContext.getArguments().get(1)) {
            case "blacklist" -> switch (commandContext.getCommand().getName()) {
                case "add" ->
                        Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist add <username> <reason_preset> <reason> [-i] [-p] [-s]");
                case "remove" ->
                        Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist remove <id-blacklist>");
                default -> Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard blacklist <add>/<remove>");
            };
            case "whitelist" -> switch (commandContext.getCommand().getName()) {
                case "add" -> Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist add <username>");
                case "remove" -> Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist remove <username>");
                default -> Component.text("§d▲ §lV4GUARD §7Correct usage: /v4guard whitelist <add>/<remove>");
            };
            default ->
                    Component.text("§d▲ §lV4GUARD §7Unrecognized command. Please use /v4guard <whitelist>/<blacklist>");
        };
    }
}
