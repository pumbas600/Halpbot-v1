package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public interface ParameterAnnotationContext
{
    Set<TypeContext<? extends Annotation>> afterAnnotations();

    default boolean comesAfter(TypeContext<? extends Annotation> annotationType) {
        return this.afterAnnotations().contains(annotationType);
    }

    void addAfterAnnotation(TypeContext<? extends Annotation> afterAnnotation);

    Set<TypeContext<? extends Annotation>> conflictingAnnotations();

    void conflictingAnnotations(Set<TypeContext<? extends Annotation>> conflictingAnnotations);

    Set<TypeContext<?>> allowedTypes();

    void allowedTypes(Set<TypeContext<?>> allowedTypes);

    default boolean isValidType(TypeContext<?> typeContext) {
        return this.allowedTypes()
                .stream()
                .anyMatch(typeContext::childOf);
    }

    default boolean noConflictingAnnotations(List<TypeContext<? extends Annotation>> annotationTypes) {
        return annotationTypes
                .stream()
                .noneMatch(annotationType -> this.conflictingAnnotations().contains(annotationType));
    }
}
