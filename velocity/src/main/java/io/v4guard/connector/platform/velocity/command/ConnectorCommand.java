package io.v4guard.connector.platform.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import io.v4guard.connector.common.CoreInstance;
import io.v4guard.connector.common.commands.AnnotatedCommand;
import io.v4guard.connector.platform.velocity.VelocityInstance;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.List;

@Permission("v4guard.command")
@Command("v4guard|v4g")
public class ConnectorCommand implements AnnotatedCommand {

    private final VelocityInstance plugin;
    private final List<String> defaultMessage = List.of(
            "<bold><color:#cb0c9f>▲ V4GUARD</color></bold> <gray>Correct usage:</gray> <yellow>/v4guard <blacklist>/<whitelist></yellow>"
    );

    public ConnectorCommand(VelocityInstance plugin) {
        this.plugin = plugin;
    }

    @Command("")
    public void help(CommandSource source) {
        Component help = plugin.getComponentAdapter().adapt(
                CoreInstance.get()
                        .getActiveSettings()
                        .getMessage("help", defaultMessage)
        );

        if (help == null) return;
        source.sendMessage(help);
    }

}
