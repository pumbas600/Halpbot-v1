package nz.pumbas.halpbot.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.decorators.CooldownDecoratorFactory;
import nz.pumbas.halpbot.decorators.Decorator;

@Decorator(CooldownDecoratorFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cooldown
{
    long duration() default 5;

    /**
     * NOTE: cooldowns are measured in milliseconds. If you specify a cooldown of less than 1ms it will be treated as
     * 0ms
     */
    TimeUnit unit() default TimeUnit.SECONDS;
}
