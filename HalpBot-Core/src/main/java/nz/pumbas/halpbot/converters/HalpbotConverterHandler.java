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
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.objects.Tuple;
import nz.pumbas.halpbot.commands.context.MethodContext;
import nz.pumbas.halpbot.utilities.Reflect;

@Service
@Binds(ConverterHandler.class)
public class HalpbotConverterHandler implements ConverterHandler
{
    private final MultiMap<TypeContext<?>, ConverterContext> converters = new ArrayListMultiMap<>();

    private final Set<TypeContext<?>> nonCommandTypes = HartshornUtils.emptyConcurrentSet();
    private final Set<TypeContext<? extends Annotation>> nonCommandAnnotations = HartshornUtils.emptyConcurrentSet();

    /**
     * Retrieves the {@link Converter} for the specified {@link Class type} and {@link MethodContext}.
     *
     * @param type
     *         The {@link Class type} of the {@link TypeConverter}
     * @param ctx
     *         The {@link MethodContext}
     * @param <T>
     *         The type of the {@link TypeConverter}
     *
     * @return The retrieved {@link Converter}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Converter<T> from(@NotNull Class<T> type, @NotNull MethodContext ctx) {
        if (this.converters.containsKey(type)) {
            return (Converter<T>) this.retrieveConverterByAnnotation(this.converters.get(type), ctx);
        }
        return (Converter<T>) this.converters.keySet()
                .stream()
                .filter(c -> c.isAssignableFrom(type))
                .findFirst()
                .map(c -> (Object) this.retrieveConverterByAnnotation(this.converters.get(c), ctx))
                .orElse(Converters.OBJECT_CONVERTER);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <T> Converter<T> from(@NotNull ParameterContext<T> parameterContext) {
        if (this.converters.containsKey(parameterContext.type())) {
            return (Converter<T>) this.retrieveConverterByAnnotation(
                    this.converters.get(parameterContext.type()),
                    parameterContext.annotations()
                            .stream()
                            .map(annotation -> TypeContext.of(annotation.annotationType()))
                            .collect(Collectors.toList()));
        }
        return (Converter<T>) this.converters.keySet()
                .stream()
                .filter(c -> c.childOf(parameterContext.type()))
                .findFirst()
                .map(c -> (Object) this.retrieveConverterByAnnotation(this.converters.get(c), ctx))
                .orElseGet(() -> this.fallbackConverters.stream()
                        .filter(t -> this.filterFallbackConverters(t, type, ctx))
                        .findFirst()
                        .map(t -> t.getValue().getConverter())
                        .orElse(Reflect.cast(Converters.OBJECT_CONVERTER)));
    }

    /**
     * Retrieves the {@link Converter} with the corresponding annotation from the list of {@link ConverterContext}.
     *
     * @param converterContexts
     *         The list of {@link ConverterContext}
     * @param ctx
     *         The {@link MethodContext}
     *
     * @return The {@link Converter} with the corresponding annotation
     */
    private Converter<?> retrieveConverterByAnnotation(@NotNull Collection<ConverterContext> converterContexts,
                                                       @NotNull Collection<TypeContext<? extends Annotation>> annotationTypes) {
        if (converterContexts.isEmpty())
            return Converters.OBJECT_CONVERTER;

        for (ConverterContext converterContext : converterContexts) {
            if (ctx.getContextState().getAnnotationTypes().contains(converterContext.getAnnotationType())) {
                ctx.getContextState().getAnnotationTypes().remove(converterContext.getAnnotationType());
                return converterContext.getConverter();
            }
        }
        // Otherwise return the last converter
        return converterContexts.get(converterContexts.size() - 1).getConverter();
    }

    //TODO: Fix up this
    /**
     * Registers a {@link Converter} against the {@link Class type} with the specified {@link Class annotation type}.
     *
     * @param type
     *         The type of the {@link TypeConverter}
     * @param annotationType
     *         The {@link Class type} of the annotation
     * @param converter
     *         The {@link Converter} to register
     */
    @Override
    public void registerConverter(@NotNull Converter<?> converter) {
        if (!this.converters.containsKey(converter.typeContext()))
            this.converters.put(converter.typeContext(), new ArrayList<>());

        List<ConverterContext> converterContexts = this.converters.get(converter.typeContext());
        for (int i = 0; i < converterContexts.size(); i++) {
            if (0 < converterContexts.get(i).getConverter().priority().compareTo(converter.priority())) {
                converterContexts.add(i, new ConverterContext(converter.annotationType(), converter));
                return;
            }
        }
        converterContexts.add(new ConverterContext(annotationType, converter));
    }

    /**
     * Registers a {@link Converter} against the {@link Predicate filter} with the specified {@link Class annotation
     * type}.
     *
     * @param filter
     *         The {@link Predicate filter} for this {@link Converter}
     * @param annotationType
     *         The {@link Class type} of the annotation
     * @param converter
     *         The {@link Converter} to register
     */
    @Override
    public void registerConverter(@NotNull Predicate<Class<?>> filter, @NotNull Class<?> annotationType,
                                  @NotNull Converter<?> converter) {

        Tuple<Predicate<Class<?>>, ConverterContext> converterContext =
                Tuple.of(filter, new ConverterContext(annotationType, converter));

        for (int i = 0; i < this.fallbackConverters.size(); i++) {
            if (0 < this.fallbackConverters.get(i).getValue().getConverter().priority().compareTo(converter.priority())) {
                this.fallbackConverters.add(i, converterContext);
                return;
            }
        }
        this.fallbackConverters.add(converterContext);
    }

    @Override
    public void addNonCommandTypes(@NotNull Set<TypeContext<?>> types) {
        this.nonCommandTypes.addAll(types);
    }

    @Override
    public void addNonCammandAnnotations(@NotNull Set<TypeContext<? extends Annotation>> types) {
        this.nonCommandAnnotations.addAll(types);
    }

    @Override
    public boolean isCommandParameter(@NotNull ParameterContext<?> parameterContext) {
        return !this.isCommandParameter(parameterContext.type()) &&
                this.nonCommandAnnotations
                        .stream()
                        .noneMatch(annotation -> parameterContext.annotation(annotation.type()).present());
    }

    @Override
    public boolean isCommandParameter(@NotNull TypeContext<?> typeContext) {
        return !this.nonCommandTypes.contains(typeContext);
    }
}
