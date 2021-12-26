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

import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.commands.context.CommandInvocationContext;

public interface ConverterHandler
{

    default <T> ParameterConverter<T> from(Class<T> type) {
        return this.from(TypeContext.of(type), TypeContext.VOID);
    }

    default <T> ParameterConverter<T> from(ParameterContext<T> parameterContext,
                                           List<TypeContext<? extends Annotation>> sortedAnnotations)
    {
        TypeContext<?> targetAnnotationType = sortedAnnotations.isEmpty() ? TypeContext.VOID : sortedAnnotations.get(0);
        return this.from(parameterContext.type(), targetAnnotationType);
    }
    
    default <T> ParameterConverter<T> from(TypeContext<T> typeContext, CommandInvocationContext invocationContext) {
        int annotationIndex = invocationContext.currentAnnotationIndex();
        List<TypeContext<? extends Annotation>> sortedAnnotations = invocationContext.sortedAnnotations();

        TypeContext<?> targetAnnotationType = annotationIndex < sortedAnnotations.size()
                ? sortedAnnotations.get(annotationIndex)
                : TypeContext.VOID;

        invocationContext.incrementAnnotationIndex();

        return this.from(typeContext, targetAnnotationType);
    }

    default <T> ParameterConverter<T> from(Class<T> type, CommandInvocationContext invocationContext) {
        return this.from(TypeContext.of(type), invocationContext);
    }

    <T> ParameterConverter<T> from(TypeContext<T> typeContext, TypeContext<?> targetAnnotationType);

    void registerConverter(ParameterConverter<?> converter);

    void addNonCommandTypes(Set<TypeContext<?>> types);

    void addNonCammandAnnotations(Set<TypeContext<? extends Annotation>> types);

    boolean isCommandParameter(ParameterContext<?> parameterContext);

    boolean isCommandParameter(TypeContext<?> typeContext, Set<TypeContext<? extends Annotation>> annotationTypes);

    boolean isCommandParameter(TypeContext<?> typeContext);
}
