package nz.pumbas.halpbot.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.actions.cooldowns.CooldownDecoratorFactory;
import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.utilities.Duration;

@Decorator(CooldownDecoratorFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cooldown
{
    Duration duration() default @Duration(5);
}
