package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.utilities.Exceptional;

public class TypeParser<T>
{
    private final Function<ParsingContext, Exceptional<T>> parser;

    public TypeParser(Function<ParsingContext, Exceptional<T>> parser)
    {
        this.parser = parser;
    }

    public static <T> TypeParser<T> of(@NotNull Class<T> type,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser);
    }

    public static <T> TypeParser<T> of(@NotNull Predicate<Class<?>> filter,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser);
    }

    public static <T> TypeParser<T> of(@NotNull Class<T> type,
                                       @NotNull Class<? extends Annotation> annotation,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser);
    }

    public static <T> TypeParser<T> of(@NotNull Predicate<Class<?>> filter,
                                       @NotNull Class<? extends Annotation> annotation,
                                       @NotNull Function<ParsingContext, Exceptional<T>> parser) {
        return new TypeParser<>(parser);
    }

    public Function<ParsingContext, Exceptional<T>> getParser()
    {
        return this.parser;
    }
}
