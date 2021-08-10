package nz.pumbas.halpbot.parsers;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import nz.pumbas.halpbot.commands.tokens.context.MethodContext;

public interface ParserHandler
{
    /**
     * Retrieves the {@link Parser} for the {@link MethodContext}.
     *
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeParser}
     *
     * @return The retrieved {@link Parser}
     */
    @SuppressWarnings("unchecked")
    default <T> Parser<T> from(@NotNull MethodContext ctx) {
        return (Parser<T>) this.from(ctx.getContextState().getClazz(), ctx);
    }

    /**
     * Retrieves the {@link Parser} for the specified {@link Class type} and {@link MethodContext}.
     *
     * @param type
     *      The {@link Class type} of the {@link TypeParser}
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeParser}
     *
     * @return The retrieved {@link Parser}
     */
    <T> Parser<T> from(@NotNull Class<T> type, @NotNull MethodContext ctx);

    /**
     * Registers a {@link Parser} against the {@link Class type} with the specified {@link Class annotation type}.
     *
     * @param type
     *      The type of the {@link TypeParser}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param typeParser
     *      The {@link Parser} to register
     */
    void registerParser(@NotNull Class<?> type, @NotNull Class<?> annotationType, @NotNull Parser<?> typeParser);

    /**
     * Registers a {@link Parser} against the {@link Predicate filter} with the specified {@link Class annotation type}.
     *
     * @param filter
     *      The {@link Predicate filter} for this {@link Parser}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param typeParser
     *      The {@link Parser} to register
     */
    void registerParser(@NotNull Predicate<Class<?>> filter, @NotNull Class<?> annotationType,
                        @NotNull Parser<?> typeParser);
}
