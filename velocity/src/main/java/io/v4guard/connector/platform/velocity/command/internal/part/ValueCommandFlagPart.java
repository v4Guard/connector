package io.v4guard.connector.platform.velocity.command.internal.part;

import team.unnamed.commandflow.CommandContext;
import team.unnamed.commandflow.exception.ArgumentParseException;
import team.unnamed.commandflow.part.CommandPart;
import team.unnamed.commandflow.part.SinglePartWrapper;
import team.unnamed.commandflow.stack.ArgumentStack;
import team.unnamed.commandflow.stack.SimpleArgumentStack;
import team.unnamed.commandflow.stack.StackSnapshot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class ValueCommandFlagPart implements SinglePartWrapper {

    private final CommandPart part;
    private final String name;
    private final String shortName;
    private final boolean allowFullName;
    private final boolean defaultValue;

    public ValueCommandFlagPart(String shortName, boolean allowFullName, CommandPart part, boolean defaultValue) {
        this.name = part.getName();
        this.shortName = shortName;
        this.allowFullName = allowFullName;
        this.defaultValue = defaultValue;

        this.part = part;
    }

    public ValueCommandFlagPart(String shortName, CommandPart part) {
        this.name = part.getName();
        this.shortName = shortName;
        this.allowFullName = false;
        this.defaultValue = true;

        this.part = part;
    }

    @Override
    public @Nullable Component getLineRepresentation() {
        TextComponent.Builder builder = Component.text()
                .append(Component.text("["))
                .append(Component.text("-" + shortName + " "));

        if (part.getLineRepresentation() != null) {
            builder.append(part.getLineRepresentation());
        }

        builder.append(Component.text("]"));

        return builder.build();
    }

    @Override
    public CommandPart getPart() {
        return part;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void parse(CommandContext context, ArgumentStack stack, CommandPart parent) throws ArgumentParseException {
        StackSnapshot snapshot = stack.getSnapshot();

        boolean found = false;

        while (stack.hasNext()) {
            String arg = stack.next();

            if (!arg.startsWith("-")) {
                continue;
            }

            if (arg.equals("--" + name) && allowFullName) {
                found = parseValueFlag(context, stack);

                break;
            }

            if (arg.equals("-" + shortName)) {
                found = parseValueFlag(context, stack);

                break;
            }
        }

        if(defaultValue) {
            part.parse(context, new SimpleArgumentStack(Collections.singletonList("")),this);
        }

        if (!found) {
            context.setValue(this, false);
        }

        stack.applySnapshot(snapshot, false);
    }

    private boolean parseValueFlag(CommandContext context, ArgumentStack stack) {
        stack.remove();
        part.parse(context,new SimpleArgumentStack(Collections.emptyList()),this);
        return true;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean allowsFullName() {
        return allowFullName;
    }

}