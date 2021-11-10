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

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nz.pumbas.halpbot.commands.context.ContextState;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.MethodContext;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.enums.Priority;

public record TypeConverter<T>(Function<InvocationContext, Exceptional<T>> mapper,
                               Priority priority,
                                ConverterRegister register,
                                OptionType optionType)
    implements Converter<T>
{
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
    public static <T> TypeConverterBuilder<T> builder(@NotNull Class<T> type) {
        return new TypeConverterBuilder<>(type);
    }

    /**
     * Returns a {@link TypeConverterBuilder} that utilises a {@link Predicate<Class> filter}.
     *
     * @param filter
     *     The {@link Predicate<Class>} filter that matches classes to this {@link Converter}
     * @param <T>
     *     The type of the converter
     *
     * @return A {@link TypeConverterBuilder}
     */
    public static <T> TypeConverterBuilder<T> builder(@NotNull Predicate<Class<?>> filter) {
        return new TypeConverterBuilder<>(filter);
    }

    public static class TypeConverterBuilder<T>
    {

        private Class<T> type;
        private Predicate<Class<?>> filter;

        private Function<InvocationContext, Exceptional<T>> converter;
        private Priority priority = Priority.DEFAULT;
        private Class<?> annotation = Void.class;
        private OptionType optionType = OptionType.STRING;

        protected TypeConverterBuilder(@NotNull Class<T> type) {
            this.type = type;
        }

        protected TypeConverterBuilder(@NotNull Predicate<Class<?>> filter) {
            this.filter = filter;
        }

        /**
         * Specifies the parsing {@link Function}. If an error is caught in the {@link Exceptional} returned by the
         * function, then it will automatically reset the current index of the {@link MethodContext}.
         *
         * @param parser
         *     The {@link Function} to be used to parse the type
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> convert(@NotNull Function<InvocationContext, Exceptional<T>> parser) {
            this.converter = ctx -> {
                //TODO: Refactor resetting context states to leverage new design
                int currentIndex = ctx.currentIndex();
                ContextState state = ctx.contextState().copyContextState();
                Exceptional<T> result = parser.apply(ctx)
                    .caught(t -> ctx.currentIndex(currentIndex));

                // Always restore the state of parser back to what it was when it was called.
                ctx.setContextState(state);
                return result;
            };
            return this;
        }

        /**
         * Specifies the {@link Priority} of this {@link TypeConverter}. A higher priority means that it will be choosen
         * before other type parsers for the same type. By default this is {@link Priority#DEFAULT}, which will be
         * evaulated last.
         *
         * @param priority
         *     The {@link Priority} to be set for this {@link TypeConverter}
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> priority(@NotNull Priority priority) {
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
        public TypeConverterBuilder<T> annotation(@NotNull Class<? extends Annotation> annotation) {
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
         * Builds the {@link TypeConverter} with the specified information but doesn't register it with {@link ConverterHandlerImpl}.
         *
         * @return The built {@link TypeConverter}
         */
        public TypeConverter<T> build() {
            ConverterRegister register = (null == this.type)
                ? (handler, converter) -> handler.registerConverter(this.filter, this.annotation, converter)
                : (handler, converter) -> handler.registerConverter(this.type, this.annotation, converter);

            return new TypeConverter<>(this.converter, this.priority, register, this.optionType);
        }
    }
}
