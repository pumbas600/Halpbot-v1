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
    String id() default "";

    boolean isEphemeral() default false;

    Duration display() default @Duration(-1);

    int uses() default -1;

    Duration after() default @Duration(-1);

    Removal afterRemoval() default Removal.DISABLE;
}
