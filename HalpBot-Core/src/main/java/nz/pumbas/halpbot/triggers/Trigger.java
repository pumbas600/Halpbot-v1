package nz.pumbas.halpbot.triggers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.permissions.Merger;
import nz.pumbas.halpbot.utilities.Duration;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trigger
{
    String[] value();

    String description() default "";

    TriggerStrategy strategy() default TriggerStrategy.START;

    Merger merger() default Merger.OR;

    Duration display() default @Duration(-1);

    boolean isEphemeral() default false;
}
