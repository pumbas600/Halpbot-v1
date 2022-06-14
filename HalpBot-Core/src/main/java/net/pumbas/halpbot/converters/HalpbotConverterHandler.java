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

package net.pumbas.halpbot.converters;

import net.pumbas.halpbot.actions.invokable.InvocationContext;
import net.pumbas.halpbot.utilities.Reflect;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.util.ArrayListMultiMap;
import org.dockbox.hartshorn.util.MultiMap;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.ParameterContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;
import lombok.Getter;

@Service
public class HalpbotConverterHandler implements ConverterHandler {

    private static final TypeContext<Object> OBJECT_TYPE = TypeContext.of(Object.class);
    private final MultiMap<TypeContext<?>, Converter<?, ?>> converters = new ArrayListMultiMap<>();
    private final Set<TypeContext<?>> nonCommandTypes = ConcurrentHashMap.newKeySet();
    private final Set<TypeContext<? extends Annotation>> nonCommandAnnotations = ConcurrentHashMap.newKeySet();

    @Getter
    @Inject
    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    private <T, C extends InvocationContext> Result<Converter<C, T>> searchAnnotationForType(final TypeContext<T> typeContext, final TypeContext<?> targetAnnotationType) {
        return Result.of(this.converters.values()
            .stream()
            .flatMap(Collection::stream)
            .filter(converter -> converter.annotationType().equals(targetAnnotationType))
            .filter(converter -> converter.type().parentOf(typeContext))
            .findFirst()
            .map(converter -> (Converter<C, T>) converter));
    }

    @Override
    public void registerConverter(final Converter<?, ?> converter) {
        if (converter instanceof SourceConverter && converter.annotationType().isVoid())
            this.addNonCommandType(converter.type());

        this.converters.put(converter.type(), converter);
    }

    @Override
    public void addNonCommandAnnotation(final TypeContext<? extends Annotation> type) {
        this.nonCommandAnnotations.add(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C extends InvocationContext> Converter<C, T> from(final TypeContext<T> typeContext, final TypeContext<?> targetAnnotationType) {
        if (!targetAnnotationType.isVoid()) {
            final Result<Converter<C, T>> eConverter = this.searchAnnotationForType(typeContext, targetAnnotationType);
            if (eConverter.present())
                return eConverter.get();
        }

        if (!this.converters.containsKey(typeContext)) {
            // Check in case there is a key that is a parent of the type context
            // (E.g: ArrayTypeContext will equal any arrays)
            return (Converter<C, T>) this.converters.keySet()
                .stream()
                .filter(type -> type.parentOf(typeContext))
                .min(Comparator.comparing(type -> type.equals(OBJECT_TYPE))) // Prioritise types that aren't Object.class
                .map(type -> this.from(type, targetAnnotationType))
                .orElse(Reflect.cast(DefaultConverters.OBJECT_CONVERTER));
        }
        return (Converter<C, T>) this.converters.get(typeContext)
            .stream()
            .filter(converter -> converter.annotationType().equals(targetAnnotationType))
            .findFirst()
            .orElse(targetAnnotationType.isVoid()
                ? DefaultConverters.OBJECT_CONVERTER
                : this.from(typeContext, TypeContext.VOID)
            );
    }

    @Override
    public <T, C extends InvocationContext> Converter<C, T> from(final ParameterContext<T> parameterContext,
                                                                 final List<TypeContext<? extends Annotation>> sortedAnnotations)
    {
        final TypeContext<?> targetAnnotationType = sortedAnnotations.isEmpty() ? TypeContext.VOID : sortedAnnotations.get(0);
        return this.from(parameterContext.type(), targetAnnotationType);
    }

    @Override
    public void addNonCommandType(final TypeContext<?> type) {
        this.nonCommandTypes.add(type);
    }

    @Override
    public boolean isCommandParameter(final ParameterContext<?> parameterContext) {
        return this.isCommandParameter(parameterContext.type()) &&
            this.nonCommandAnnotations
                .stream()
                .noneMatch(annotation -> parameterContext.annotation(annotation.type()).present());
    }

    @Override
    public boolean isCommandParameter(final TypeContext<?> typeContext, final Set<TypeContext<? extends Annotation>> annotationTypes) {
        return this.isCommandParameter(typeContext) &&
            this.nonCommandAnnotations
                .stream()
                .noneMatch(annotationTypes::contains);
    }

    @Override
    public boolean isCommandParameter(final TypeContext<?> typeContext) {
        return !this.nonCommandTypes.contains(typeContext) &&
            this.nonCommandTypes.stream()
                .noneMatch(typeContext::childOf);
    }
}
