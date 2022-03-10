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

package net.pumbas.halpbot.converters.tokens;

import net.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import net.pumbas.halpbot.converters.Converter;
import net.pumbas.halpbot.converters.ConverterHandler;
import net.pumbas.halpbot.converters.SourceConverter;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.inject.Provided;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

@Service
public interface TokenFactory
{
    @Provided
    ConverterHandler converterHandler();

    @Provided
    ParameterAnnotationService parameterAnnotationService();

    @Provided
    InvocationContextFactory invocationContexFactory();

    @Factory
    PlaceholderToken createPlaceholder(boolean isOptional, String placeholder);

    @Factory
    ParsingToken createParsing(ParameterContext<?> parameterContext,
                               List<TypeContext<? extends Annotation>> sortedAnnotations,
                               Converter<?, ?> converter,
                               @Nullable Object defaultValue,
                               boolean isCommandParameter,
                               boolean isOptional);

    default ParsingToken createParsing(ParameterContext<?> parameterContext) {
        final ConverterHandler converterHandler = this.converterHandler();
        final ParameterAnnotationService annotationService = this.parameterAnnotationService();

        List<TypeContext<? extends Annotation>> sortedAnnotations =
            annotationService.sortAndFilter(
                parameterContext.annotations()
                    .stream()
                    .map(annotation -> TypeContext.of(annotation.annotationType())));

        Converter<CommandInvocationContext, ?> converter = converterHandler.from(parameterContext, sortedAnnotations);
        boolean isCommandParameter = !(converter instanceof SourceConverter);
        boolean isOptional = false;
        @Nullable Object defaultValue = null;

        Exceptional<Unrequired> unrequired = parameterContext.annotation(Unrequired.class);

        if (unrequired.present()) {
            isOptional = true;
            CommandInvocationContext invocationContext = this.invocationContexFactory().command(unrequired.get().value());
            invocationContext.update(parameterContext, sortedAnnotations);

            defaultValue = ParsingToken.parseDefaultValue(converter, invocationContext);
        }

        return this.createParsing(
            parameterContext,
            sortedAnnotations,
            converter,
            defaultValue,
            isCommandParameter,
            isOptional);
    }
}
