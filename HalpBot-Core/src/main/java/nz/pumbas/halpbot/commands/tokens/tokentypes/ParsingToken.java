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

package nz.pumbas.halpbot.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import nz.pumbas.halpbot.commands.context.MethodContext;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.TypeConverter;
import nz.pumbas.halpbot.utilities.Reflect;

/**
 * {@link ParsingToken Parsing tokens} are tokens which have a specific type and can parse an inputted {@link String} to this type.
 */
public interface ParsingToken extends Token
{
    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @NotNull
    Annotation[] getAnnotations();

    /**
     * @return An {@link List} of the {@link Class types} of the annotations on this {@link ParsingToken}
     */
    @NotNull
    List<Class<? extends Annotation>> getAnnotationTypes();

    /**
     * @return The required {@link Type} of this {@link ParsingToken}
     */
    @NotNull
    Type getType();

    /**
     * @return The {@link TypeConverter} for this token
     */
    @NotNull
    Converter<?> getConverter();

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Nullable
    Object getDefaultValue();

    /**
     * @return If this {@link ParsingToken} is a command parameter. If not, then it must extract information from the
     * source event or command adapter.
     */
    boolean isCommandParameter();

    /**
     * @return The parameter name for this {@link ParsingToken}.
     */
    @NotNull
    String getParameterName();

    /**
     * Retrieves the {@link Annotation} on this {@link ParsingToken} based on the specified {@link Class annotation
     * type}.
     *
     * @param annotationType
     *     The {@link Class annotation type}
     *
     * @return The {@link Annotation} with the matching {@link Class annotation type}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        int index = this.getAnnotationTypes().indexOf(annotationType);
        if (-1 == index)
            return null;
        return (T) this.getAnnotations()[index];
    }

    /**
     * Parses the {@link MethodContext} of the default value.
     *
     * @param ctx
     *     {@link MethodContext} containing the default value to be parsed into an {@link Object} using the token's
     *     {@link TypeConverter}
     *
     * @return The parsed {@link Object default value}
     */
    @Nullable
    default Object parseDefaultValue(@NotNull MethodContext ctx) {
        if ("${Default}".equalsIgnoreCase(ctx.getOriginal()))
            return Reflect.getDefaultValue(ctx.getContextState().getClazz());
        if (String.class.isAssignableFrom(ctx.getContextState().getClazz()))
            return ctx.getOriginal();
        return this.getConverter()
            .getMapper()
            .apply(ctx)
            .rethrow()
            .get();

    }
}
