package nz.pumbas.halpbot.converters.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When used to annotate a converter, it specifies that the converters type or any type that extends it are not
 * parameters which are passed in when someone uses a command and is instead something that is extracted based on the
 * event, or other like-information.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface NonCommandAnnotation
{
}
