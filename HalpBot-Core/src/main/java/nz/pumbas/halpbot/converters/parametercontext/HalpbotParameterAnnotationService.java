package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import lombok.Getter;

@Service
@Binds(ParameterAnnotationService.class)
public class HalpbotParameterAnnotationService implements ParameterAnnotationService
{
    private final Map<TypeContext<? extends Annotation>, ParameterAnnotationContext> parameterAnnotationContextMap
            = new ConcurrentHashMap<>();

    @Inject
    @Getter private ApplicationContext applicationContext;

    @Inject
    @Getter private ParameterAnnotationContextFactory factory;

    @Getter
    private final Comparator<TypeContext<? extends Annotation>> comparator = (typeA, typeB) -> {
        ParameterAnnotationContext contextA = this.get(typeA);
        ParameterAnnotationContext contextB = this.get(typeB);
        
        if (contextA.comesAfter(typeB))
            return 1;
        else if (contextB.comesAfter(typeA))
            return -1;
        else if (contextA.afterAnnotations().size() > contextB.afterAnnotations().size())
            return 1;
        else if (contextB.afterAnnotations().size() > contextA.afterAnnotations().size())
            return -1;
        return 0;
    };

    @Override
    public ParameterAnnotationContext get(TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap
                .getOrDefault(annotationType, HalpbotParameterAnnotationContext.GENERIC);
    }

    @Override
    public boolean isRegisteredParameterAnnotation(TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap.containsKey(annotationType);
    }

    @Override
    public void add(TypeContext<? extends Annotation> annotationType,
                    ParameterAnnotationContext annotationContext)
    {
        this.parameterAnnotationContextMap.put(annotationType, annotationContext);
    }

    @Override
    public boolean contains(TypeContext<? extends Annotation> annotationType) {
        return this.parameterAnnotationContextMap.containsKey(annotationType);
    }
}
