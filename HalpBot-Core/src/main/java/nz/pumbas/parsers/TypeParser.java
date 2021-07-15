package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.enums.Priority;

public class TypeParser<T>
{
    private final Function<ParsingContext, Exceptional<T>> parser;
    private final Priority priority;

    public TypeParser(Function<ParsingContext, Exceptional<T>> parser, Priority priority)
    {
        this.parser = parser;
        this.priority = priority;
    }

    public static <T> TypeParser<T> of(@NotNull Class<T> type,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser, Priority.DEFAULT);
    }

    public static <T> TypeParser<T> of(@NotNull Predicate<Class<?>> filter,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser, Priority.DEFAULT);
    }

    public static <T> TypeParser<T> of(@NotNull Class<T> type,
                                       @NotNull Class<? extends Annotation> annotation, Priority priority,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser, priority);
    }

    public static <T> TypeParser<T> of(@NotNull Predicate<Class<?>> filter,
                                       @NotNull Class<? extends Annotation> annotation, Priority priority,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser, priority);
    }

    public static <T> TypeParserBuilder<T> builder(@NotNull Class<T> type) {
        return new TypeParserBuilder<>(type);
    }

    public static <T> TypeParserBuilder<T> builder(@NotNull Predicate<Class<?>> filter) {
        return new TypeParserBuilder<>(filter);
    }

    public Function<ParsingContext, Exceptional<T>> getParser()
    {
        return this.parser;
    }

    public Priority getPriority()
    {
        return this.priority;
    }

    public static class TypeParserBuilder<T> {

        private Class<T> type;
        private Predicate<Class<?>> filter;
        private Function<ParsingContext, Exceptional<T>> parser;
        private Priority priority = Priority.DEFAULT;
        private Class<? extends Annotation> annotation;

        public TypeParserBuilder() { }

        protected TypeParserBuilder(@NotNull Class<T> type) {
            this.type = type;
        }

        protected TypeParserBuilder(@NotNull Predicate<Class<?>> filter) {
            this.filter = filter;
        }

        public TypeParserBuilder<T> convert(@NotNull Function<ParsingContext, Exceptional<T>> parser) {
            this.parser = parser;
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

        public TypeParser<T> register() {
            return new TypeParser<>(this.parser, this.priority);
        }
    }
}
