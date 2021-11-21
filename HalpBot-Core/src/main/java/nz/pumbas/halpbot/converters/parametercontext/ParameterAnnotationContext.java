package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

public interface ParameterAnnotationContext
{
    @NotNull
    Set<TypeContext<? extends Annotation>> afterAnnotations();

    void addAfterAnnotation(@NotNull TypeContext<? extends Annotation> afterAnnotation);

    @NotNull
    Set<TypeContext<? extends Annotation>> conflictingAnnotations();

    void conflictingAnnotations(@NotNull Set<TypeContext<? extends Annotation>> conflictingAnnotations);

    @NotNull
    Set<TypeContext<?>> allowedTypes();

    void allowedTypes(@NotNull Set<TypeContext<?>> allowedTypes);

    default boolean isValidType(@NotNull TypeContext<?> typeContext) {
        return this.allowedTypes()
                .stream()
                .anyMatch(typeContext::childOf);
    }

    default boolean noConflictingAnnotations(@NotNull List<TypeContext<? extends Annotation>> annotationTypes) {
        return annotationTypes
                .stream()
                .noneMatch(annotationType -> this.conflictingAnnotations().contains(annotationType));
    }
}
