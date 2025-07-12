package io.v4guard.connector.common.command.internal.modifier;


import io.v4guard.connector.common.command.internal.annotations.CommandFlag;
import io.v4guard.connector.common.command.internal.part.ValueCommandFlagPart;
import team.unnamed.commandflow.annotated.part.PartModifier;
import team.unnamed.commandflow.part.CommandPart;

import java.lang.annotation.Annotation;
import java.util.List;

public class ValueCommandFlagModifier implements PartModifier {

    @Override
    public CommandPart modify(CommandPart original, List<? extends Annotation> modifiers) {
        CommandFlag flag = getModifier(modifiers, CommandFlag.class);

        String shortName = flag != null ? flag.value() : original.getName();

        return new ValueCommandFlagPart(shortName, flag != null && flag.allowFullName(), original, flag != null && flag.hasDefaultValue());
    }

}
