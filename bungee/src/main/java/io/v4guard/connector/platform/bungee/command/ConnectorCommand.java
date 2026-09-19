package io.v4guard.connector.platform.bungee.command;


import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.commands.AnnotatedCommand;
import io.v4guard.connector.platform.bungee.BungeeInstance;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;

@Permission("v4guard.command")
@Command("v4guard|v4g")
public class ConnectorCommand implements AnnotatedCommand {

    private final BungeeInstance plugin;
    private final List<String> defaultMessage = List.of(
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard <blacklist>/<whitelist></yellow>"
    );

    public ConnectorCommand(BungeeInstance plugin) {
        this.plugin = plugin;
    }

    @Command("")
    public void help(CommandSender source) {
        BaseComponent[] help = plugin.getComponentAdapter().adapt(
                CoreInstance.get()
                        .getActiveSettings()
                        .getMessage("help", defaultMessage)
        );

        if (help == null) return;
        source.sendMessage(help);
    }

}
