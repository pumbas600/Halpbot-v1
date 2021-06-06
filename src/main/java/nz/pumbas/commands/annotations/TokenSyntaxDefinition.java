package nz.pumbas.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.commands.Order;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TokenSyntaxDefinition
{
    /**
     * Species the {@link Order} in which the method annotated with this will be executed.
     *
     * @return The {@link Order}
     */
    Order value() default Order.NORMAL;
}
