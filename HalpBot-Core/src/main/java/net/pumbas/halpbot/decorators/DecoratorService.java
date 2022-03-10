/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.decorators;

import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import net.pumbas.halpbot.actions.invokable.InvocationContext;
import net.pumbas.halpbot.utilities.Reflect;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.AnnotatedElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.domain.tuple.Tuple;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Map<TypeContext<? extends Annotation>, List<Annotation>> parentDecorators = this.decorators(actionInvokable.executable().parent());
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

        ActionInvokable<C> previous;
        for (Tuple<TypeContext<? extends Annotation>, Annotation> entry : entries) {
            DecoratorFactory<?, ?, ?> factory = this.decorator(entry.getKey());
            if (factory instanceof ActionInvokableDecoratorFactory actionInvokableDecoratorFactory) {
                previous = actionInvokable;
                actionInvokable = (ActionInvokable<C>) actionInvokableDecoratorFactory.decorate(
                    actionInvokable,
                    entry.getValue());
                if (actionInvokable == null) {
                    this.applicationContext().log()
                        .error("There was an error while creating the decorator %s with %s"
                            .formatted(entry.getKey().qualifiedName(), entry.getValue().toString()));
                    actionInvokable = previous;
                }
            } else this.applicationContext().log()
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

    @SuppressWarnings("unchecked")
    default <T extends ActionInvokable<?>> List<T> decorators(ActionInvokable<?> actionInvokable, Class<T> type) {
        List<T> decorators = new ArrayList<>();

        while (actionInvokable instanceof ActionInvokableDecorator decorator) {
            if (type.isAssignableFrom(actionInvokable.getClass()))
                decorators.add((T) actionInvokable);
            actionInvokable = decorator.actionInvokable();
        }
        return decorators;
    }

    default ActionInvokable<?> root(ActionInvokable<?> actionInvokable) {
        while (actionInvokable instanceof ActionInvokableDecorator decorator) {
            actionInvokable = decorator.actionInvokable();
        }
        return actionInvokable;
    }
}
