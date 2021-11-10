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

package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.commands.context.InvocationContext;
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
     * @return The required {@link TypeContext} of this {@link ParsingToken}
     */
    @NotNull
    ParameterContext<?> parameterContext();

    /**
     * @return The {@link TypeConverter} for this token
     */
    @NotNull
    Converter<?> converter();

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Nullable
    Object defaultValue();

    /**
     * @return If this {@link ParsingToken} is a command parameter. If not, then it must extract information from the
     * source event or command adapter.
     */
    boolean isCommandParameter();

    /**
     * Parses the {@link MethodContext} of the default value.
     *
     * @param invocationContext
     *     {@link MethodContext} containing the default value to be parsed into an {@link Object} using the token's
     *     {@link TypeConverter}
     *
     * @return The parsed {@link Object default value}
     */
    @Nullable
    static Object parseDefaultValue(@NotNull Converter<?> converter, @NotNull InvocationContext invocationContext) {
        //TODO: Move ${Default} parsing to the converter, so that it can be used in commands too
        if ("${Default}".equalsIgnoreCase(invocationContext.content()))
            return Reflect.getDefaultValue(invocationContext.currentParameter().type().type());
        if (invocationContext.currentParameter().type().childOf(String.class))
            return invocationContext.content();
        return converter.apply(invocationContext)
            .rethrow()
            .get();

    }
}
