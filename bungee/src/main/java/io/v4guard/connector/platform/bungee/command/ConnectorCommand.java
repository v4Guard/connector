package io.v4guard.connector.platform.bungee.command;


import io.v4guard.connector.platform.bungee.command.sub.BlacklistCommand;
import io.v4guard.connector.platform.bungee.command.sub.WhitelistCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.commandflow.annotated.annotation.SubCommandClasses;

@SubCommandClasses(
        {
                WhitelistCommand.class,
                BlacklistCommand.class
        }
)
@Command(names = "v4Guard", permission = "v4guard.command")
public class ConnectorCommand implements CommandClass {

    @Command(names = "")
    public void help(@Sender CommandSender source) {
        source.sendMessage(new ComponentBuilder("§d▲ §lV4GUARD §7Correct usage: /v4guard <blacklist>/<whitelist>").create());


    }

}
