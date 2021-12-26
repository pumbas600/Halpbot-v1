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
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import nz.pumbas.halpbot.commands.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.exceptions.IllegalConverterException;
import nz.pumbas.halpbot.utilities.enums.Priority;

public record TypeConverter<T>(TypeContext<T> type,
                               TypeContext<? extends Annotation> annotationType,
                               Function<CommandInvocationContext, Exceptional<T>> mapper,
                               Priority priority,
                               OptionType optionType,
                               boolean requiresHalpbotEvent)
    implements Converter<T>
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

    public static class TypeConverterBuilder<T>
    {
        private final TypeContext<T> type;
        private @Nullable Function<CommandInvocationContext, Exceptional<T>> converter;
        private @Nullable Class<? extends Annotation> annotation;

        private Priority priority = Priority.DEFAULT;
        private OptionType optionType = OptionType.STRING;
        private boolean requiresHalpbotEvent;

        protected TypeConverterBuilder(TypeContext<T> type) {
            this.type = type;
        }

        /**
         * Specifies the parsing {@link Function}. If an error is caught in the {@link Exceptional} returned by the
         * function, then it will automatically reset the current index of the {@link MethodContext}.
         *
         * @param mapper
         *     The {@link Function} to be used to parse the type
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> convert(Function<CommandInvocationContext, Exceptional<T>> mapper) {
            this.converter = mapper;
            return this;
        }

        /**
         * Specifies the {@link Priority} of this {@link TypeConverter}. A higher priority means that it will be choosen
         * before other type parsers for the same type. By default, this is {@link Priority#DEFAULT}, which will be
         * evaulated last.
         *
         * @param priority
         *     The {@link Priority} to be set for this {@link TypeConverter}
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * The {@link Class type} of the annotation which needs to be present on the type for this {@link TypeConverter}
         * to be called. If no annotation is specified, then this type parser can be called irrespective of the
         * annotations present.
         *
         * @param annotation
         *     The {@link Class type} of the annotation that needs to be present for this type parser to be called
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> annotation(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        /**
         * Specifies the JDA {@link OptionType} to map this type to, when building slash commands. If no option type
         * is specified, then it will default to {@link OptionType#STRING}.
         *
         * @param optionType
         *     The JDA {@link OptionType}
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> optionType(OptionType optionType) {
            this.optionType = optionType;
            return this;
        }

        /**
         * Specifies that this converter requires there to be a {@link nz.pumbas.halpbot.events.HalpbotEvent}. If
         * this event is null in the {@link CommandInvocationContext} then it will automatically return an exceptional
         * containing a {@link NullPointerException} and the converter function will NOT be called. By default, this
         * is false.
         *
         * @param isRequired
         *      If the halpbot event is required to be present
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> requiresHalpbotEvent(boolean isRequired) {
            this.requiresHalpbotEvent = isRequired;
            return this;
        }

        /**
         * Builds the {@link TypeConverter} with the specified information but doesn't register it with {@link HalpbotConverterHandler}.
         *
         * @return The built {@link TypeConverter}
         */
        public TypeConverter<T> build() {
            if (this.converter == null)
                throw new IllegalConverterException(
                        "You must specify a converting function before building the converter");
            return new TypeConverter<>(
                    this.type,
                    TypeContext.of(this.annotation),
                    this.converter,
                    this.priority,
                    this.optionType,
                    this.requiresHalpbotEvent);
        }
    }
}
