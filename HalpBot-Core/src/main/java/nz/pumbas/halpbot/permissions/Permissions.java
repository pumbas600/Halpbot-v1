package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.decorators.DecoratorMerge;
import nz.pumbas.halpbot.decorators.Order;

@Decorator(value = PermissionDecoratorFactory.class, order = Order.FIRST, merge = DecoratorMerge.KEEP_BOTH)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permissions
{
    String[] permissions() default {};

    Permission[] user() default {};

    Permission[] self() default {};

    boolean canInteract() default false;

    Merger merger() default Merger.AND;
}
