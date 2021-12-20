package nz.pumbas.halpbot.decorators;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Method
{
    Class<? extends DecoratorFactory<?, ?, ?>> type();

    String name();
}
