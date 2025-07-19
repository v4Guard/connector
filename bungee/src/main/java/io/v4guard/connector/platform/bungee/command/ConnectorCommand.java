package io.v4guard.connector.platform.bungee.command;


import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.platform.bungee.command.sub.BlacklistCommand;
import io.v4guard.connector.platform.bungee.command.sub.WhitelistCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.SubCommandClasses;

import java.util.List;

@SubCommandClasses({
    WhitelistCommand.class,
    BlacklistCommand.class
})
@Command(names = "v4Guard", permission = "v4guard.command")
public class ConnectorCommand implements CommandClass {

    private final List<String> defaultMessage = List.of("§d▲ §lV4GUARD §7Correct usage: /v4guard <blacklist>/<whitelist>");

    @Command(names = "")
    public void help(@Sender CommandSender source) {
        CoreInstance.get().getActiveSettings()
                .getMessage("commandHelp", defaultMessage)
                .forEach(line -> source.sendMessage(new ComponentBuilder(line).create()));
    }

}
