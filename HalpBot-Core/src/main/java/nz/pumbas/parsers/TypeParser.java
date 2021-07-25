package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.enums.Priority;

public class TypeParser<T> implements Parser<T>
{
    private Function<ParsingContext, Exceptional<T>> parser;
    private BiFunction<Type, ParsingContext, Exceptional<T>> biParser;
    private final Priority priority;

    public TypeParser(BiFunction<Type, ParsingContext, Exceptional<T>> biParser, Priority priority)
    {
        this.biParser = biParser;
        this.priority = priority;
    }

    public TypeParser(Function<ParsingContext, Exceptional<T>> parser, Priority priority)
    {
        this.parser = parser;
        this.priority = priority;
    }

    /**
     * Returns a {@link TypeParserBuilder} that parses an element to the specified {@link Class type}.
     *
     * @param type
     *      The {@link Class type} of the parsed element
     * @param <T>
     *      The type of the parsed element
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
     *      The {@link Predicate<Class>} filter that matches classes to this {@link Parser}
     * @param <T>
     *      The type of the parsed element
     *
     * @return A {@link TypeParserBuilder}
     */
    public static <T> TypeParserBuilder<T> builder(@NotNull Predicate<Class<?>> filter) {
        return new TypeParserBuilder<>(filter);
    }

    /**
     * @return The {@link BiFunction} for this {@link Parser}
     */
    @Override
    public @Nullable BiFunction<Type, ParsingContext, Exceptional<T>> biParser()
    {
        return this.biParser;
    }

    /**
     * @return The {@link Function} for this {@link Parser}
     */
    @Override
    public @Nullable Function<ParsingContext, Exceptional<T>> parser()
    {
        return this.parser;
    }

    /**
     * @return The {@link Priority} associated with this {@link Parser}
     */
    @Override
    public Priority priority()
    {
        return this.priority;
    }



    protected static class TypeParserBuilder<T> {

        private Class<T> type;
        private Predicate<Class<?>> filter;

        private Function<ParsingContext, Exceptional<T>> parser;
        private BiFunction<Type, ParsingContext, Exceptional<T>> biParser;
        private Priority priority = Priority.DEFAULT;
        private Class<?> annotation = Void.class;

        protected TypeParserBuilder(@NotNull Class<T> type) {
            this.type = type;
        }

        protected TypeParserBuilder(@NotNull Predicate<Class<?>> filter) {
            this.filter = filter;
        }

        public TypeParserBuilder<T> convert(@NotNull Function<ParsingContext, Exceptional<T>> parser) {
            this.parser = ctx -> {
                int currentIndex = ctx.getCurrentIndex();
                return parser.apply(ctx)
                    .absent(() -> ctx.setCurrentIndex(currentIndex));
            };
            return this;
        }

        public TypeParserBuilder<T> convert(@NotNull BiFunction<Type, ParsingContext, Exceptional<T>> biParser) {
            this.biParser = (type, ctx) -> {
                int currentIndex = ctx.getCurrentIndex();
                return biParser.apply(type, ctx)
                    .absent(() -> ctx.setCurrentIndex(currentIndex));
            };
            return this;
        }

        public TypeParserBuilder<T> priority(@NotNull Priority priority) {
            this.priority = priority;
            return this;
        }

        public TypeParserBuilder<T> annotation(@NotNull Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        public TypeParser<T> build() {
            if (null == this.parser)
                return new TypeParser<>(this.biParser, this.priority);
            return new TypeParser<>(this.parser, this.priority);
        }

        public TypeParser<T> register() {
            TypeParser<T> typeParser = this.build();

            if (null == this.type)
                Parsers.registerParser(this.filter, typeParser);
            else Parsers.registerParser(this.type, this.annotation, typeParser);

            return typeParser;
        }
    }
}
