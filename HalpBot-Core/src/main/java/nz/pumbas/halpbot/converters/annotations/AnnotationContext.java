package nz.pumbas.halpbot.converters.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface AnnotationContext
{
    /**
     * The annotations, which if present, should be processed before this one.
     */
    Class<? extends Annotation>[] dependencies() default {};

    /**
     * The annotations that cannot be used in conjunction with this one
     */
    Class<? extends Annotation>[] conflictingAnnotations() default {};

    /**
     * The parameter types that this annotation can be used on.
     */
    Class<?>[] allowedType() default Object.class;
}
