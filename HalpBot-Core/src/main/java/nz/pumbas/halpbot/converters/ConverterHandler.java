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

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.FieldContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.commands.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.annotations.Ignore;
import nz.pumbas.halpbot.converters.annotations.NonCommandAnnotation;

public interface ConverterHandler extends ContextCarrier, Enableable
{
    @Override
    @SuppressWarnings("unchecked")
    default void enable() {
        Set<TypeContext<? extends Annotation>> nonCommandAnnotations = this.applicationContext().environment()
                .types(NonCommandAnnotation.class)
                .stream()
                .map(type -> (TypeContext<? extends Annotation>) type)
                .collect(Collectors.toSet());

        this.addNonCammandAnnotations(nonCommandAnnotations);
        this.applicationContext().log().info("Registered %d noncommand annotations".formatted(nonCommandAnnotations.size()));
    }

    default <T, C extends InvocationContext> Converter<C, T> from(Class<T> type) {
        return this.from(TypeContext.of(type), TypeContext.VOID);
    }

    default <T, C extends InvocationContext> Converter<C, T> from(ParameterContext<T> parameterContext, List<TypeContext<? extends Annotation>> sortedAnnotations)
    {
        TypeContext<?> targetAnnotationType = sortedAnnotations.isEmpty() ? TypeContext.VOID : sortedAnnotations.get(0);
        return this.from(parameterContext.type(), targetAnnotationType);
    }
    
    default <T, C extends InvocationContext> Converter<C, T> from(TypeContext<T> typeContext, CommandInvocationContext invocationContext) {
        int annotationIndex = invocationContext.currentAnnotationIndex();
        List<TypeContext<? extends Annotation>> sortedAnnotations = invocationContext.sortedAnnotations();

        TypeContext<?> targetAnnotationType = annotationIndex < sortedAnnotations.size()
                ? sortedAnnotations.get(annotationIndex)
                : TypeContext.VOID;

        invocationContext.incrementAnnotationIndex();

        return this.from(typeContext, targetAnnotationType);
    }

    default <T, C extends InvocationContext> Converter<C, T> from(Class<T> type, CommandInvocationContext invocationContext) {
        return this.from(TypeContext.of(type), invocationContext);
    }

    <T, C extends InvocationContext> Converter<C, T> from(TypeContext<T> typeContext, TypeContext<?> targetAnnotationType);

    @SuppressWarnings("rawtypes")
    default <T> void register(TypeContext<T> type) {
        int count = 0;
        List<FieldContext<Converter>> converters = type.fieldsOf(Converter.class);

        for (FieldContext<Converter> fieldContext : converters) {
            if (fieldContext.annotation(Ignore.class).present())
                continue;

            if (!fieldContext.isStatic() || !fieldContext.isPublic() || !fieldContext.isFinal())
                this.applicationContext().log().warn("The converter %s in %s needs to be static, public and final"
                        .formatted(fieldContext.name(), type.qualifiedName()));

            else {
                fieldContext.get(null).present(this::registerConverter);
                count++;
            }
        }
        this.applicationContext().log().info("Registered %d converters found in %s".formatted(count, type.qualifiedName()));
    }

    void registerConverter(Converter<?, ?> converter);

    void addNonCommandType(TypeContext<?> type);

    default void addNonCommandTypes(Set<TypeContext<?>> types) {
        types.forEach(this::addNonCommandType);
    }

    void addNonCammandAnnotation(TypeContext<? extends Annotation> type);

    default void addNonCammandAnnotations(Set<TypeContext<? extends Annotation>> types) {
        types.forEach(this::addNonCammandAnnotation);
    }

    boolean isCommandParameter(ParameterContext<?> parameterContext);

    boolean isCommandParameter(TypeContext<?> typeContext, Set<TypeContext<? extends Annotation>> annotationTypes);

    boolean isCommandParameter(TypeContext<?> typeContext);
}
