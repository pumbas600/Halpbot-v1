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

    public Function<ParsingContext, Exceptional<T>> getParser()
    {
        return this.parser;
    }

    public Priority getPriority()
    {
        return this.priority;
    }
}
