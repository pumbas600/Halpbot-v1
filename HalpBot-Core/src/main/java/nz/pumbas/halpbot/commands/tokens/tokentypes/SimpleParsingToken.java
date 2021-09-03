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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.commands.context.MethodContext;
import nz.pumbas.halpbot.commands.tokens.CommandManager;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.converters.TypeConverter;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class SimpleParsingToken implements ParsingToken
{
    private final Type type;
    private final Annotation[] annotations;
    private final List<Class<? extends Annotation>> annotationTypes;
    private final boolean isOptional;
    private final Converter<?> converter;
    private final Object defaultValue;
    private final boolean isCommandParameter;
    private final String parameterName;

    public SimpleParsingToken(Type type, Annotation[] annotations, String parameterName) {
        this.type = type;
        this.annotations = annotations;
        this.annotationTypes = Stream.of(annotations)
            .map(Annotation::annotationType)
            .collect(Collectors.toUnmodifiableList());

        Unrequired unrequired = this.getAnnotation(Unrequired.class);
        this.isOptional = null != unrequired;

        String defaultValue = this.isOptional ? unrequired.value() : "${Default}";
        MethodContext ctx = MethodContext.of(defaultValue, this);

        this.converter = HalpbotUtils.context().get(ConverterHandler.class).from(ctx);
        this.defaultValue = this.parseDefaultValue(ctx);

        this.isCommandParameter = CommandManager.isCommandParameter(type, annotations);
        this.parameterName = parameterName;
    }

    /**
     * @return If this {@link Token} is optional or not
     */
    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @Override
    public @NotNull Annotation[] getAnnotations() {
        return this.annotations;
    }

    /**
     * @return A copy of the {@link List} of the {@link Class types} of the annotations on this {@link ParsingToken}
     */
    @Override
    public @NotNull List<Class<? extends Annotation>> getAnnotationTypes() {
        return new ArrayList<>(this.annotationTypes);
    }

    /**
     * @return The required {@link Type} of this {@link ParsingToken}
     */
    @Override
    public @NotNull Type getType() {
        return this.type;
    }

    /**
     * @return The {@link TypeConverter} for this token
     */
    @Override
    public @NotNull Converter<?> getConverter() {
        return this.converter;
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    public @Nullable Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * @return If this {@link ParsingToken} is a command parameter. If not, then it must extract information from the
     *     source event or command adapter.
     */
    @Override
    public boolean isCommandParameter() {
        return this.isCommandParameter;
    }

    /**
     * @return The parameter name for this {@link ParsingToken}.
     */
    @Override
    public @NotNull String getParameterName() {
        return this.parameterName;
    }
}
