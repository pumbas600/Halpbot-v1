package nz.pumbas.halpbot.triggers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.permissions.Require;
import nz.pumbas.halpbot.utilities.Duration;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trigger
{
    String[] value();

    String description() default "";

    TriggerStrategy strategy() default TriggerStrategy.START;

    /**
     * If using {@link Require#ALL}, this will force the trigger strategy to be {@link TriggerStrategy#ANYWHERE}
     * regardless of what is manually set.
     */
    Require merger() default Require.ANY;

    Duration display() default @Duration(-1);

    boolean isEphemeral() default false;
}
