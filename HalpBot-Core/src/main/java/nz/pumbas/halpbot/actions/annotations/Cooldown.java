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
     * NOTE: You can't use a time unit smaller than seconds. Trying to do so will cause an error to be thrown when
     * generating the action.
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
