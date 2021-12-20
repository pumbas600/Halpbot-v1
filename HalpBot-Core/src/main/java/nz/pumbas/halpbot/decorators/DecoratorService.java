package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface DecoratorService extends Enableable, ContextCarrier
{
    @Override
    @SuppressWarnings("unchecked")
    default void enable() {
        Collection<TypeContext<?>> decorators = this.applicationContext().environment()
                .types(Decorator.class)
                .stream()
                .filter(type -> type.childOf(Annotation.class))
                .toList();

        for (TypeContext<?> decorator : decorators) {
            this.register((TypeContext<? extends Annotation>) decorator);
        }

        this.applicationContext().log().info("Registered %d decorators".formatted(decorators.size()));
    }

    void register(TypeContext<? extends Annotation> decoratedAnnotation);

    @Nullable
    default DecoratorFactory<?, ?, ?> decorator(Annotation annotation) {
        return this.decorator(TypeContext.of(annotation.annotationType()));
    }

    @Nullable
    DecoratorFactory<?, ?, ?> decorator(TypeContext<? extends Annotation> decoratedAnnotation);
}
