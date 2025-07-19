package io.v4guard.connector.platform.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import io.v4guard.connector.platform.velocity.command.sub.BlacklistCommand;
import io.v4guard.connector.platform.velocity.command.sub.WhitelistCommand;
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

    private final VelocityInstance plugin;
    private final List<String> defaultMessage = List.of(
            "§d▲ §lV4GUARD §7Correct usage: /v4guard <blacklist>/<whitelist>"
    );

    public ConnectorCommand(VelocityInstance plugin) {
        this.plugin = plugin;
    }

    @Command(names = "")
    public void help(@Sender CommandSource source) {
        CoreInstance.get()
                .getActiveSettings()
                .getMessage("help", defaultMessage)
                .forEach(line -> source.sendMessage(plugin.getLegacyComponentSerializer().deserialize(line)));
    }

}
