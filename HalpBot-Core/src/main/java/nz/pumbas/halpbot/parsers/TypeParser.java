package nz.pumbas.halpbot.parsers;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

import nz.pumbas.halpbot.commands.tokens.context.ContextState;
import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.utilities.Exceptional;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.enums.Priority;

public class TypeParser<T> implements Parser<T>
{
    private final Function<MethodContext, Exceptional<T>> parser;
    private final Priority priority;

    public TypeParser(Function<MethodContext, Exceptional<T>> parser, Priority priority) {
        this.parser = parser;
        this.priority = priority;
    }

    /**
     * Returns a {@link TypeParserBuilder} that parses an element to the specified {@link Class type}.
     *
     * @param type
     *     The {@link Class type} of the parsed element
     * @param <T>
     *     The type of the parsed element
     *
     * @return A {@link TypeParserBuilder}
     */
    public static <T> TypeParserBuilder<T> builder(@NotNull Class<T> type) {
        return new TypeParserBuilder<>(type);
    }

    /**
     * Returns a {@link TypeParserBuilder} that utilises a {@link Predicate<Class> filter}.
     *
     * @param filter
     *     The {@link Predicate<Class>} filter that matches classes to this {@link Parser}
     * @param <T>
     *     The type of the parsed element
     *
     * @return A {@link TypeParserBuilder}
     */
    public static <T> TypeParserBuilder<T> builder(@NotNull Predicate<Class<?>> filter) {
        return new TypeParserBuilder<>(filter);
    }

    /**
     * @return The {@link Function} for this {@link Parser}
     */
    @Override
    public Function<MethodContext, Exceptional<T>> getMapper() {
        return this.parser;
    }

    /**
     * @return The {@link Priority} associated with this {@link Parser}
     */
    @Override
    public Priority getPriority() {
        return this.priority;
    }


    protected static class TypeParserBuilder<T>
    {

        private Class<T> type;
        private Predicate<Class<?>> filter;

        private Function<MethodContext, Exceptional<T>> parser;
        private Priority priority = Priority.DEFAULT;
        private Class<?> annotation = Void.class;

        protected TypeParserBuilder(@NotNull Class<T> type) {
            this.type = type;
        }

        protected TypeParserBuilder(@NotNull Predicate<Class<?>> filter) {
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
        public TypeParserBuilder<T> convert(@NotNull Function<MethodContext, Exceptional<T>> parser) {
            this.parser = ctx -> {
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
         * Specifies the {@link Priority} of this {@link TypeParser}. A higher priority means that it will be choosen
         * before other type parsers for the same type. By default this is {@link Priority#DEFAULT}, which will be
         * evaulated last.
         *
         * @param priority
         *      The {@link Priority} to be set for this {@link TypeParser}
         *
         * @return Itself for chaining
         */
        public TypeParserBuilder<T> priority(@NotNull Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * The {@link Class type} of the annotation which needs to be present on the type for this {@link TypeParser}
         * to be called. If no annotation is specified, then this type parser can be called irrespective of the
         * annotations present.
         *
         * @param annotation
         *      The {@link Class type} of the annotation that needs to be present for this type parser to be called
         *
         * @return Itself for chaining
         */
        public TypeParserBuilder<T> annotation(@NotNull Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        /**
         * Builds the {@link TypeParser} with the specified information but doesn't register it with {@link ParserHandlerImpl}.
         *
         * @return The built {@link TypeParser}
         */
        public TypeParser<T> build() {
            return new TypeParser<>(this.parser, this.priority);
        }

        /**
         * Builds the {@link TypeParser} and automatically registers it with {@link ParserHandlerImpl}. This makes it
         * available to be retrieved using {@link ParserHandlerImpl#from}.
         *
         * @return The built {@link TypeParser}
         */
        public TypeParser<T> register() {
            TypeParser<T> typeParser = this.build();

            if (null == this.type)
                HalpbotUtils.context().get(ParserHandler.class)
                    .registerParser(this.filter, this.annotation, typeParser);
            else HalpbotUtils.context().get(ParserHandler.class)
                    .registerParser(this.type, this.annotation, typeParser);

            return typeParser;
        }
    }
}
