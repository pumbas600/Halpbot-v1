package nz.pumbas.halpbot.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.commands.CommandType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Command
{
    String prefix() default "";

    String alias() default "";

    String command() default "";

    String description() default "";

    String permission() default "";

    long[] restrictedTo() default {};

    Class<?>[] reflections() default {};

    CommandType commandType() default CommandType.MESSAGE;
}
