package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Set;

//TODO: Determine what's wrong with @Factory and @Bound annotations
@Factory
public interface ParameterAnnotationContextFactory
{
    @Bound
    ParameterAnnotationContext get(@NotNull Set<TypeContext<? extends Annotation>> afterAnnotations,
                                   @NotNull Set<TypeContext<? extends Annotation>> conflictingAnnotations,
                                   @NotNull Set<TypeContext<?>> allowedTypes);
}
