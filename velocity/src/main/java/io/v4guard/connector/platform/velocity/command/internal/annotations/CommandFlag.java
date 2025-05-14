package io.v4guard.connector.platform.velocity.command.internal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandFlag {
    /**
     * The short name used for this flag
     * @return A string representing the short name for this flag
     */
    String value();

    boolean hasDefaultValue();

    boolean allowFullName() default false;

}
