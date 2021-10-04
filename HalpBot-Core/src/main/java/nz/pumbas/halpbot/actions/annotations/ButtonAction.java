package nz.pumbas.halpbot.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.actions.ResponseType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ButtonAction
{
    ResponseType responseType() default ResponseType.REPLY;

    boolean isEphemeral() default false;
}
