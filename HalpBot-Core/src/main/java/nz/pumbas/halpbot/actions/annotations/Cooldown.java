package nz.pumbas.halpbot.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.decorators.CooldownDecoratorFactory;
import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.utilities.Duration;

@Decorator(CooldownDecoratorFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cooldown
{
    Duration duration() default @Duration(5);
}
