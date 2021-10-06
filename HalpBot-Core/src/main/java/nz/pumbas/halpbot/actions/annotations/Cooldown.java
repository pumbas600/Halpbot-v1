package nz.pumbas.halpbot.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cooldown
{
    long duration() default 5;

    /**
     * NOTE: You can't use a time unit smaller than milliseconds. Trying to specify a time in microseconds or
     * nanoseconds will result in an error.
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
