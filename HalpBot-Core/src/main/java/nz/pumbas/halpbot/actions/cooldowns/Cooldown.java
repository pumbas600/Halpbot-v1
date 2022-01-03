package nz.pumbas.halpbot.actions.cooldowns;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.utilities.Duration;

@Decorator(CooldownDecoratorFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Cooldown
{
    Duration duration() default @Duration(5);
}
