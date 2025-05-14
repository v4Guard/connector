package io.v4guard.connector.platform.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import io.v4guard.connector.platform.velocity.command.sub.BlacklistCommand;
import io.v4guard.connector.platform.velocity.command.sub.WhitelistCommand;
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
    public void help(@Sender CommandSource source) {
        source.sendPlainMessage("Commands: ");
        source.sendPlainMessage("/connector whitelist <add/remove> <player>");
        source.sendPlainMessage("/connector blacklist <add/remove> <player>");

    }

}
