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
    long listeningDuration() default -1;

    /**
     * The time unit for the listening duration.
     */
    TimeUnit listeningDurationUnit() default TimeUnit.MINUTES;

    /**
     * Sets that the action can only be used once. After that first use, the callback will then be
     * automatically removed, along with all other callbacks on the same message. By default, this is false.
     */
    boolean isSingleUse() default false;

    /**
     * Sets how long the response should be displayed for in seconds. If this is less than 1, then it will not be
     * removed.
     */
    long displayDuration() default -1;
}
