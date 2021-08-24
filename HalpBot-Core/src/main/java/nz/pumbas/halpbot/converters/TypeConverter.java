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

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

import nz.pumbas.halpbot.commands.tokens.context.ContextState;
import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.enums.Priority;

public class TypeConverter<T> implements Converter<T>
{
    private final Function<MethodContext, Exceptional<T>> converter;
    private final Priority priority;
    private final ConverterRegister register;

    public TypeConverter(Function<MethodContext, Exceptional<T>> converter, Priority priority, ConverterRegister register) {
        this.converter = converter;
        this.priority = priority;
        this.register = register;
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

    /**
     * @return The {@link Function} for this {@link Converter}
     */
    @Override
    public Function<MethodContext, Exceptional<T>> getMapper() {
        return this.converter;
    }

    /**
     * @return The {@link Priority} associated with this {@link Converter}
     */
    @Override
    public Priority getPriority() {
        return this.priority;
    }

    /**
     * @return The {@link ConverterRegister} for this converter, describing how to register it to a {@link ConverterHandler}
     */
    @Override
    public ConverterRegister getRegister() {
        return this.register;
    }

    public static class TypeConverterBuilder<T>
    {

        private Class<T> type;
        private Predicate<Class<?>> filter;

        private Function<MethodContext, Exceptional<T>> converter;
        private Priority priority = Priority.DEFAULT;
        private Class<?> annotation = Void.class;

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
         *      The {@link Function} to be used to parse the type
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> convert(@NotNull Function<MethodContext, Exceptional<T>> parser) {
            this.converter = ctx -> {
                int currentIndex = ctx.getCurrentIndex();
                ContextState state = ctx.getContextState().copyContextState();
                Exceptional<T> result = parser.apply(ctx)
                    .caught(t -> ctx.setCurrentIndex(currentIndex));

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
         *      The {@link Priority} to be set for this {@link TypeConverter}
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
         *      The {@link Class type} of the annotation that needs to be present for this type parser to be called
         *
         * @return Itself for chaining
         */
        public TypeConverterBuilder<T> annotation(@NotNull Class<? extends Annotation> annotation) {
            this.annotation = annotation;
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

            return new TypeConverter<>(this.converter, this.priority, register);
        }

        /**
         * Builds the {@link TypeConverter} and automatically registers it with {@link ConverterHandlerImpl}. This makes it
         * available to be retrieved using {@link ConverterHandlerImpl#from}.
         *
         * @return The built {@link TypeConverter}
         */
        public TypeConverter<T> register() {
            TypeConverter<T> typeParser = this.build();

            if (null == this.type)
                HalpbotUtils.context().get(ConverterHandler.class)
                    .registerConverter(this.filter, this.annotation, typeParser);
            else HalpbotUtils.context().get(ConverterHandler.class)
                    .registerConverter(this.type, this.annotation, typeParser);

            return typeParser;
        }
    }
}
