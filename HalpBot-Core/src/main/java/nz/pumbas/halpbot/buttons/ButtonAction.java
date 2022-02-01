package nz.pumbas.halpbot.buttons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.utilities.Duration;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ButtonAction
{
    String id();

    boolean isEphemeral() default false;

    Duration display() default @Duration(-1);

    int maxUses() default -1;

    Duration removeAfter() default @Duration(-1);

    Removal afterRemoval() default Removal.DISABLE;
}
