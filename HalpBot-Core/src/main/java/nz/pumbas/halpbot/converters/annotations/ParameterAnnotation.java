package nz.pumbas.halpbot.converters.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nz.pumbas.halpbot.converters.HalpbotParameterAnnotationContext;
import nz.pumbas.halpbot.converters.ParameterAnnotationContext;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ParameterAnnotation
{
    /**
     * The annotations, which if present, should be processed before this one. Note: That this is equivalent to
     * setting the {@link ParameterAnnotation#before()} of the specified annotations to this.
     */
    Class<? extends Annotation>[] after() default {};

    /**
     * The annotations which if present, should be processed afterAnnotations this one. Note: That this is equivalent to
     * setting the {@link ParameterAnnotation#after()} of the specified annotations to this. The main purpose of this
     * is to allow you to add custom annotations that are processed before the built-in ones, as you don't have
     * access to their respective {@link ParameterAnnotation}.
     */
    Class<? extends Annotation>[] before() default {};

    /**
     * The annotations that cannot be used in conjunction with this one.
     */
    Class<? extends Annotation>[] conflictingAnnotations() default {};

    /**
     * The parameter types that this annotation can be used on. By default, it can be used on any type.
     */
    Class<?>[] allowedType() default Object.class;

    //TODO: Create factory binding for constructor when available
    /**
     * Allows you to specify a custom context to use for this parameter annotation. By default, this is the built-in
     * {@link HalpbotParameterAnnotationContext}. Note that any implementation should have the following constructor:
     * <pre>{@code
     *     @NotNull TypeContext<? extends Annotation> annotation,
     *     @NotNull Set<TypeContext<? extends Annotation>> afterAnnotations,
     *     @NotNull Set<TypeContext<? extends Annotation>> conflictingAnnotations,
     *     @NotNull Set<TypeContext<?>> allowedTypes
     * }</pre>
     */
    Class<? extends ParameterAnnotationContext> context() default HalpbotParameterAnnotationContext.class;
}
