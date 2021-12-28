package nz.pumbas.halpbot.decorators.time;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.decorators.Order;
import nz.pumbas.halpbot.utilities.LogLevel;

@Decorator(value = TimeDecoratorFactory.class, order = Order.FIRST)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Time
{
    LogLevel value() default LogLevel.INFO;
}
