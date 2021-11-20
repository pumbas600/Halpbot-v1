package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;

@Service
@Binds(ParameterAnnotationService.class)
public class HalpbotParameterAnnotationService implements ParameterAnnotationService
{
    @Inject
    @Getter private ApplicationContext applicationContext;

    @Inject
    @Getter private ParameterAnnotationContextFactory factory;

    private final Map<TypeContext<? extends Annotation>, ParameterAnnotationContext> parameterAnnotationContextMap
            = HartshornUtils.emptyConcurrentMap();

    @Override
    @NotNull
    public ParameterAnnotationContext get(@NotNull TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap
                .getOrDefault(annotationType, HalpbotParameterAnnotationContext.GENERIC);
    }

    @Override
    public void add(@NotNull TypeContext<? extends Annotation> annotationType,
                    @NotNull ParameterAnnotationContext annotationContext)
    {
        this.parameterAnnotationContextMap.put(annotationType, annotationContext);
    }

    @Override
    public boolean contains(@NotNull TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap.containsKey(annotationType);
    }
}
