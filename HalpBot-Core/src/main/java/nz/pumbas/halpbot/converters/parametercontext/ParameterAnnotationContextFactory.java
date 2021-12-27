package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Set;

@Service
public interface ParameterAnnotationContextFactory
{
    @Factory
    ParameterAnnotationContext create(@NotNull Set<TypeContext<? extends Annotation>> afterAnnotations,
                                      @NotNull Set<TypeContext<? extends Annotation>> conflictingAnnotations,
                                      @NotNull Set<TypeContext<?>> allowedTypes);
}
