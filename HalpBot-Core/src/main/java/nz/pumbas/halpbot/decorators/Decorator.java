package nz.pumbas.halpbot.decorators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Decorator
{
    /**
     * The factory that constructors the decorator
     */
    Class<? extends DecoratorFactory<?, ?, ?>> value();

    Order order() default Order.DEFAULT;
}
