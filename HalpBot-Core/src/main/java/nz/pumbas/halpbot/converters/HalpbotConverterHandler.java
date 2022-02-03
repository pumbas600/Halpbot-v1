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

package nz.pumbas.halpbot.converters;

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.utilities.Reflect;

@Singleton
@ComponentBinding(ConverterHandler.class)
public class HalpbotConverterHandler implements ConverterHandler
{
    private final MultiMap<TypeContext<?>, Converter<?, ?>> converters = new ArrayListMultiMap<>();
    private static final TypeContext<Object> OBJECT_TYPE = TypeContext.of(Object.class);

    private final Set<TypeContext<?>> nonCommandTypes = HartshornUtils.emptyConcurrentSet();
    private final Set<TypeContext<? extends Annotation>> nonCommandAnnotations = HartshornUtils.emptyConcurrentSet();

    @Getter
    @Inject private ApplicationContext applicationContext;

    @Override
    public void registerConverter(Converter<?, ?> converter) {
        if (converter instanceof SourceConverter && converter.annotationType().isVoid())
            this.addNonCommandType(converter.type());

        this.converters.put(converter.type(), converter);
    }

    @Override
    public <T, C extends InvocationContext> Converter<C, T> from(ParameterContext<T> parameterContext,
                                                                 List<TypeContext<? extends Annotation>> sortedAnnotations)
    {
        TypeContext<?> targetAnnotationType = sortedAnnotations.isEmpty() ? TypeContext.VOID : sortedAnnotations.get(0);
        return this.from(parameterContext.type(), targetAnnotationType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, C extends InvocationContext> Converter<C, T> from(TypeContext<T> typeContext, TypeContext<?> targetAnnotationType) {
        if (!targetAnnotationType.isVoid()) {
            Exceptional<Converter<C, T>> eConverter = this.searchAnnotationForType(typeContext, targetAnnotationType);
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

    @SuppressWarnings("unchecked")
    private <T, C extends InvocationContext> Exceptional<Converter<C, T>> searchAnnotationForType(TypeContext<T> typeContext, TypeContext<?> targetAnnotationType) {
        return Exceptional.of(this.converters.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(converter -> converter.annotationType().equals(targetAnnotationType))
                .filter(converter -> converter.type().parentOf(typeContext))
                .findFirst()
                .map(converter -> (Converter<C, T>) converter));
    }

    @Override
    public void addNonCommandType(TypeContext<?> type) {
        this.nonCommandTypes.add(type);
    }

    @Override
    public void addNonCammandAnnotation(TypeContext<? extends Annotation> type) {
        this.nonCommandAnnotations.add(type);
    }

    @Override
    public boolean isCommandParameter(ParameterContext<?> parameterContext) {
        return this.isCommandParameter(parameterContext.type()) &&
                this.nonCommandAnnotations
                        .stream()
                        .noneMatch(annotation -> parameterContext.annotation(annotation.type()).present());
    }

    @Override
    public boolean isCommandParameter(TypeContext<?> typeContext, Set<TypeContext<? extends Annotation>> annotationTypes) {
        return this.isCommandParameter(typeContext) &&
                this.nonCommandAnnotations
                        .stream()
                        .noneMatch(annotationTypes::contains);
    };

    @Override
    public boolean isCommandParameter(TypeContext<?> typeContext) {
        return !this.nonCommandTypes.contains(typeContext) &&
                this.nonCommandTypes.stream()
                        .noneMatch(typeContext::childOf);
    }
}
