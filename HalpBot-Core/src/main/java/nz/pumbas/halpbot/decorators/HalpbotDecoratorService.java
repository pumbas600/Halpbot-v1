package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;

@Singleton
@ComponentBinding(DecoratorService.class)
public class HalpbotDecoratorService implements DecoratorService
{
    private final Map<TypeContext<? extends Annotation>, DecoratorFactory<?, ?, ?>> decorators = new ConcurrentHashMap<>();

    @Getter @Inject private ApplicationContext applicationContext;

    @Override
    public void register(TypeContext<? extends Annotation> decoratedAnnotation) {
        Class<? extends DecoratorFactory<?, ?, ?>> factoryType = decoratedAnnotation.annotation(Decorator.class)
                .map(Decorator::value)
                .get();

        DecoratorFactory<?, ?, ?> factory = this.applicationContext.get(factoryType);
        if (factory == null) {
            this.applicationContext.log().error("There was an error retrieving the decorator factory %s"
                    .formatted(factoryType.getCanonicalName()));
            return;
        }

        this.decorators.put(decoratedAnnotation, factory);
    }

    @Override
    @Nullable
    public DecoratorFactory<?, ?, ?> decorator(TypeContext<? extends Annotation> decoratedAnnotation) {
        return this.decorators.get(decoratedAnnotation);
    }
}
