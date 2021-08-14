package nz.pumbas.halpbot.converters;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import nz.pumbas.halpbot.commands.tokens.context.MethodContext;

public interface ConverterHandler
{
    /**
     * Retrieves the {@link Converter} for the {@link MethodContext}.
     *
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeConverter}
     *
     * @return The retrieved {@link Converter}
     */
    @SuppressWarnings("unchecked")
    default <T> Converter<T> from(@NotNull MethodContext ctx) {
        return (Converter<T>) this.from(ctx.getContextState().getClazz(), ctx);
    }

    /**
     * Retrieves the {@link Converter} for the specified {@link Class type} and {@link MethodContext}.
     *
     * @param type
     *      The {@link Class type} of the {@link TypeConverter}
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeConverter}
     *
     * @return The retrieved {@link Converter}
     */
    <T> Converter<T> from(@NotNull Class<T> type, @NotNull MethodContext ctx);

    /**
     * Registers a {@link Converter} against the {@link Class type} with the specified {@link Class annotation type}.
     *
     * @param type
     *      The type of the {@link TypeConverter}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param converter
     *      The {@link Converter} to register
     */
    void registerConverter(@NotNull Class<?> type, @NotNull Class<?> annotationType, @NotNull Converter<?> converter);

    /**
     * Registers a {@link Converter} against the {@link Predicate filter} with the specified {@link Class annotation type}.
     *
     * @param filter
     *      The {@link Predicate filter} for this {@link Converter}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param converter
     *      The {@link Converter} to register
     */
    void registerConverter(@NotNull Predicate<Class<?>> filter, @NotNull Class<?> annotationType,
                           @NotNull Converter<?> converter);
}
