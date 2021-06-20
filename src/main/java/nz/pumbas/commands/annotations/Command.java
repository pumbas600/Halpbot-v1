package nz.pumbas.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
    String prefix() default "";

    String alias();

    String command() default "";

    String description() default "";

    String permission() default "";

    long[] restrictedTo() default {};

    Class<?>[] reflections() default {};
}
