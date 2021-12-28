package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.AnnotatedElementContext;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.context.element.TypedElementContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.domain.tuple.Tuple;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.utilities.Reflect;

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

    //TODO: Test this
    @SuppressWarnings("unchecked")
    default <C extends InvocationContext> ActionInvokable<C> decorate(ActionInvokable<C> actionInvokable) {
        Map<TypeContext<? extends Annotation>, List<Annotation>> parentDecorators = this.decorators(this.parent(actionInvokable.executable()));
        Map<TypeContext<? extends Annotation>, List<Annotation>> decorators = this.decorators(actionInvokable.executable());

        // Merge the parent and action decorators using the DecoratorMerge specified
        for (TypeContext<? extends Annotation> annotation : parentDecorators.keySet()) {
            Decorator decorator = annotation.annotation(Decorator.class).get();
            decorators.merge(annotation, parentDecorators.get(annotation), decorator.merge()::merge);
        }

        List<Tuple<TypeContext<? extends Annotation>, Annotation>> entries = Reflect.cast(
                decorators.entrySet()
                        .stream()
                        .flatMap((entry) -> entry.getValue()
                                .stream()
                                .map((annotation) -> Tuple.of(entry.getKey(), annotation)))
                        .sorted(Comparator.comparing((entry) -> -entry.getKey().annotation(Decorator.class).get().order().ordinal()))
                        .toList());

        for (Tuple<TypeContext<? extends Annotation>, Annotation> entry : entries) {
            DecoratorFactory<?, ?, ?> factory = this.decorator(entry.getKey());
            if (factory instanceof ActionInvokableDecoratorFactory actionInvokableDecoratorFactory) {
                actionInvokable = (ActionInvokable<C>) actionInvokableDecoratorFactory.decorate(
                        actionInvokable,
                        entry.getValue());
            }
            else this.applicationContext().log()
                    .error("The command %s is annotated with the decorator %s, but this does not support commands"
                            .formatted(actionInvokable.executable().qualifiedName(), entry.getKey().qualifiedName()));
        }

        return actionInvokable;
    }

    default Map<TypeContext<? extends Annotation>, List<Annotation>> decorators(AnnotatedElementContext<?> annotatedElementContext) {
        return annotatedElementContext.annotations()
                .stream()
                .filter((annotation) -> TypeContext.of(annotation.annotationType()).annotation(Decorator.class).present())
                .collect(Collectors.groupingBy(annotation -> TypeContext.of(annotation.annotationType())));
    }

    //TODO: Remove once this becomes available in HH:
    private TypeContext<?> parent(ExecutableElementContext<?> executable) {
        if (executable instanceof MethodContext methodContext)
            return methodContext.parent();
        return ((TypedElementContext<?>) executable).type();
    }

    default int depth(ActionInvokable<?> actionInvokable) {
        int depth = 0;
        while (actionInvokable instanceof ActionInvokableDecorator decorator) {
            depth++;
            actionInvokable = decorator.actionInvokable();
        }
        return depth;
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

    default ActionInvokable<?> root(ActionInvokable<?> actionInvokable) {
        while (actionInvokable instanceof ActionInvokableDecorator decorator) {
            actionInvokable = decorator.actionInvokable();
        }
        return actionInvokable;
    }
}
