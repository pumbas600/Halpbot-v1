package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface DecoratorService extends Enableable, ContextCarrier
{
    @Override
    default void enable() {
        Collection<TypeContext<?>> decorators = this.applicationContext().environment()
                .types(Decorator.class)
                .stream()
                .filter(type -> type.childOf(Annotation.class))
                .toList();

        for (TypeContext<?> decorator : decorators) {
            this.register(decorator);
        }

        this.applicationContext().log().info("Registered %d decorators".formatted(decorators.size()));
    }

    void register(TypeContext<?> decoratedAnnotation);

    DecoratorContext decorator(Annotation annotation);
}
