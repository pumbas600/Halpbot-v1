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

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;

public record TypeConverter<T>(TypeContext<T> type,
                               TypeContext<? extends Annotation> annotationType,
                               Function<CommandInvocationContext, Exceptional<T>> mapper,
                               OptionType optionType,
                               boolean requiresHalpbotEvent)
    implements ParameterConverter<T>
{
    public static <T> TypeConverterBuilder<T> builder(Class<T> type) {
        return builder(TypeContext.of(type));
    }

    /**
     * Returns a {@link TypeConverterBuilder} from the {@link Class type} which builds the {@link TypeConverter}.
     *
     * @param type
     *     The {@link Class type} of the converter
     * @param <T>
     *     The type of the converter
     *
     * @return A {@link TypeConverterBuilder}
     */
    public static <T> TypeConverterBuilder<T> builder(TypeContext<T> type) {
        return new TypeConverterBuilder<>(type);
    }

    public static class TypeConverterBuilder<T> extends ConverterBuilder<TypeConverter<T>, CommandInvocationContext, T>
    {
        protected TypeConverterBuilder(TypeContext<T> type) {
            super(type);
        }

        /**
         * Specifies the parsing {@link Function}. If an error is caught in the {@link Exceptional} returned by the
         * function, then it will automatically reset the current index of the {@link CommandInvocationContext}.
         *
         * @param mapper
         *     The {@link Function} to be used to parse the type
         *
         * @return Itself for chaining
         */
        @Override
        public TypeConverterBuilder<T> convert(Function<CommandInvocationContext, Exceptional<T>> mapper) {
            //Overriden to provide more specific javadoc
            super.convert(mapper);
            return this;
        }

        /**
         * Builds the {@link TypeConverter} with the specified information but doesn't register it with {@link HalpbotConverterHandler}.
         *
         * @return The built {@link TypeConverter}
         */
        @Override
        public TypeConverter<T> build() {
            this.assertConverterSet();
            return new TypeConverter<>(
                    this.type,
                    TypeContext.of(this.annotation),
                    this.converter,
                    this.optionType,
                    this.requiresHalpbotEvent);
        }
    }
}
