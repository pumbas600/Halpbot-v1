package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.enums.Priority;

public class TypeParser<T>
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

    public static <T> TypeParserBuilder<T> builder(@NotNull Class<T> type) {
        return new TypeParserBuilder<>(type);
    }

    public static <T> TypeParserBuilder<T> builder(@NotNull Predicate<Class<?>> filter) {
        return new TypeParserBuilder<>(filter);
    }

    public BiFunction<Type, ParsingContext, Exceptional<T>> getBiParser()
    {
        return this.biParser;
    }

    public Function<ParsingContext, Exceptional<T>> getParser()
    {
        return this.parser;
    }

    public Exceptional<T> apply(@NotNull Type type, @NotNull ParsingContext ctx) {
        if (null != this.biParser)
            return this.biParser.apply(type, ctx);
        return this.parser.apply(ctx);
    }

    public Exceptional<T> apply(@NotNull ParsingContext ctx) {
        if (null != this.parser)
            return this.parser.apply(ctx);
        return this.biParser.apply(ctx.type(), ctx);
    }

    public Priority getPriority()
    {
        return this.priority;
    }

    public static class TypeParserBuilder<T> {

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
                ctx.saveState(this);
                return parser.apply(ctx)
                    .absent(() -> ctx.restoreState(this));
            };
            return this;
        }

        public TypeParserBuilder<T> convert(@NotNull BiFunction<Type, ParsingContext, Exceptional<T>> biParser) {
            this.biParser = (type, ctx) -> {
                ctx.saveState(this);
                return biParser.apply(type, ctx)
                    .absent(() -> ctx.restoreState(this));
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
