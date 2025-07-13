package io.v4guard.connector.common.command.internal.part;

import team.unnamed.commandflow.CommandContext;
import team.unnamed.commandflow.annotated.part.PartFactory;
import team.unnamed.commandflow.exception.ArgumentParseException;
import team.unnamed.commandflow.part.ArgumentPart;
import team.unnamed.commandflow.part.CommandPart;
import team.unnamed.commandflow.stack.ArgumentStack;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class FlagPartFactory implements PartFactory {

    @Override
    public CommandPart createPart(String s, List<? extends Annotation> list) {

        return new ArgumentPart() {
            @Override
            public List<?> parseValue(CommandContext commandContext, ArgumentStack argumentStack, CommandPart commandPart) throws ArgumentParseException {
                return Collections.singletonList(!argumentStack.hasNext());
            }

            @Override
            public String getName() {
                return s;
            }
        };
    }
}
