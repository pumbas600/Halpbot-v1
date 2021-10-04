package nz.pumbas.halpbot.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Action
{
    /**
     * The permissions a user must have to use this action.
     */
    String[] permissions() default {};

    /**
     * The duration that the action will be listened for before being removed. Setting it as -1 means it will never
     * stop listening.
     */
    long listeningDuration() default 10L;

    /**
     * The time unit for the listening duration.
     */
    TimeUnit listeningDurationUnit() default TimeUnit.MINUTES;

    /**
     * The cooldown between uses by a user. Setting it as -1 means that there is no cooldown.
     */
    long cooldown() default -1L;

    /**
     * The time unit for the cooldown.
     */
    TimeUnit cooldownUnit() default TimeUnit.MINUTES;
}
