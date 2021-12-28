package nz.pumbas.halpbot.permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.decorators.DecoratorMerge;
import nz.pumbas.halpbot.decorators.Order;

@Decorator(value = PermissionDecoratorFactory.class, order = Order.FIRST, merge = DecoratorMerge.KEEP_BOTH)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission
{
    String[] value();
}
