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

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import nz.pumbas.halpbot.commands.tokens.context.MethodContext;

public interface ConverterHandler
{
    /**
     * Retrieves the {@link Converter} for the {@link MethodContext}.
     *
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeConverter}
     *
     * @return The retrieved {@link Converter}
     */
    @SuppressWarnings("unchecked")
    default <T> Converter<T> from(@NotNull MethodContext ctx) {
        return (Converter<T>) this.from(ctx.getContextState().getClazz(), ctx);
    }

    /**
     * Retrieves the {@link Converter} for the specified {@link Class type} and {@link MethodContext}.
     *
     * @param type
     *      The {@link Class type} of the {@link TypeConverter}
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeConverter}
     *
     * @return The retrieved {@link Converter}
     */
    <T> Converter<T> from(@NotNull Class<T> type, @NotNull MethodContext ctx);

    /**
     * Registers a {@link Converter} against the {@link Class type} with the specified {@link Class annotation type}.
     *
     * @param type
     *      The type of the {@link TypeConverter}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param converter
     *      The {@link Converter} to register
     */
    void registerConverter(@NotNull Class<?> type, @NotNull Class<?> annotationType, @NotNull Converter<?> converter);

    /**
     * Registers a {@link Converter} against the {@link Predicate filter} with the specified {@link Class annotation type}.
     *
     * @param filter
     *      The {@link Predicate filter} for this {@link Converter}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param converter
     *      The {@link Converter} to register
     */
    void registerConverter(@NotNull Predicate<Class<?>> filter, @NotNull Class<?> annotationType,
                           @NotNull Converter<?> converter);
}
