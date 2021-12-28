package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;

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

    //TODO: Check parent class for shared decorators
    @SuppressWarnings("unchecked")
    default <C extends InvocationContext> ActionInvokable<C> decorate(ActionInvokable<C> actionInvokable) {
        List<? extends TypeContext<? extends Annotation>> decoratedAnnotations = actionInvokable.executable().annotations()
                .stream()
                .map(annotation -> TypeContext.of(annotation.annotationType()))
                .filter(annotation -> annotation.annotation(Decorator.class).present())
                .sorted(Comparator.comparing(annotation -> -annotation.annotation(Decorator.class).get().order().ordinal()))
                .toList();

        for (TypeContext<? extends Annotation> decoratedAnnotation : decoratedAnnotations) {
            DecoratorFactory<?, ?, ?> factory = this.decorator(decoratedAnnotation);
            if (factory instanceof ActionInvokableDecoratorFactory actionInvokableDecoratorFactory) {
                actionInvokable = (ActionInvokable<C>) actionInvokableDecoratorFactory.decorate(
                        actionInvokable,
                        actionInvokable.executable().annotation(decoratedAnnotation).get());
            }
            else this.applicationContext().log()
                    .error("The command %s is annotated with the decorator %s, but this does not support commands"
                            .formatted(actionInvokable.executable().qualifiedName(), decoratedAnnotation.qualifiedName()));
        }

        return actionInvokable;
    }

    @SuppressWarnings("unchecked")
    default <T extends ActionInvokable<?>> Exceptional<T> decorator(ActionInvokable<?> actionInvokable, Class<T> type) {
        while (actionInvokable instanceof ActionInvokableDecorator decorator) {
            if (type.isAssignableFrom(actionInvokable.getClass()))
                return Exceptional.of((T) actionInvokable);
            actionInvokable = decorator.actionInvokable();
        }
        return Exceptional.empty();
    }
}
