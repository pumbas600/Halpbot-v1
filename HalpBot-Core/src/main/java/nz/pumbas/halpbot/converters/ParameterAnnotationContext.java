package nz.pumbas.halpbot.converters;

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

    boolean isValidType(@NotNull TypeContext<?> typeContext);

    boolean noConflictingAnnotations(@NotNull List<Annotation> annotations);
}
