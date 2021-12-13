package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.converters.annotations.Any;
import nz.pumbas.halpbot.converters.types.ArrayTypeContext;

@SuppressWarnings("ConstantDeclaredInInterface")
public interface ParameterAnnotationContext
{
    TypeContext<Any> ANY = TypeContext.of(Any.class);

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
                //TODO: Replace with type.parentOf(typeContext)
                .anyMatch(type -> type instanceof ArrayTypeContext
                        ? type.childOf(typeContext) : typeContext.childOf(type));
    }

    default boolean noConflictingAnnotations(List<TypeContext<? extends Annotation>> annotationTypes) {
        return (!this.conflictingAnnotations().contains(ANY) || annotationTypes.size() == 1) &&
                annotationTypes.stream()
                        .noneMatch(annotationType -> this.conflictingAnnotations().contains(annotationType));
    }
}
