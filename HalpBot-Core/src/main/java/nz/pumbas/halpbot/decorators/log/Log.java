package nz.pumbas.halpbot.decorators.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.decorators.Order;
import nz.pumbas.halpbot.utilities.LogLevel;

@Decorator(value = LogDecoratorFactory.class, order = Order.LATE)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log
{
    LogLevel value() default LogLevel.INFO;
}
