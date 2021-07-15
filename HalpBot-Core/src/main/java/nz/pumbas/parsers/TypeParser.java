package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.objects.Result;

public class TypeParser<T>
{
    private final Class<T> type;
    private final Function<ParsingContext, Result<T>> parser;

    public TypeParser(Class<T> type, Function<ParsingContext, Result<T>> parser)
    {
        this.type = type;
        this.parser = parser;
    }

    public static <T> TypeParser<T> of(@NotNull Class<T> type, @NotNull Function<ParsingContext, Result<T>> parser) {
        return new TypeParser<>(type, parser);
    }

    public static <T> TypeParser<T> of(@NotNull Class<T> type, @NotNull Class<? extends Annotation> annotation,
                                    @NotNull Function<ParsingContext, Result<T>> parser) {
        return new TypeParser<>(type, parser);
    }

    public Class<T> getType()
    {
        return this.type;
    }

    public Function<ParsingContext, Result<T>> getParser()
    {
        return this.parser;
    }
}
