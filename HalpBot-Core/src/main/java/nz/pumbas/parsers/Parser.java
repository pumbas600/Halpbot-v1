package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.enums.Priority;

public interface Parser<T>
{
    /**
     * @return The {@link BiFunction} for this {@link Parser}
     */
    @Nullable
    BiFunction<Type, ParsingContext, Exceptional<T>> biParser();

    /**
     * @return The {@link Function} for this {@link Parser}
     */
    @Nullable
    Function<ParsingContext, Exceptional<T>> parser();

    /**
     * @return The {@link Priority} associated with this {@link Parser}
     */
    Priority priority();

    /**
     * Applies the relevant parser function and returns the {@link Exceptional result}.
     *
     * @param type
     *      The {@link Type} of the current element being parsed
     * @param ctx
     *      The associated {@link ParsingContext}
     *
     * @return The returned {@link Exceptional}
     */
    default Exceptional<T> apply(@NotNull Type type, @NotNull ParsingContext ctx) {
        if (null != this.biParser())
            return this.biParser().apply(type, ctx);
        if (null != this.parser())
            return this.parser().apply(ctx);
        return Exceptional.empty();
    }

    /**
     * Applies the relevant parser function and returns the {@link Exceptional result}.
     *
     * @param ctx
     *      The associated {@link ParsingContext}
     *
     * @return The returned {@link Exceptional}
     */
    default Exceptional<T> apply(@NotNull ParsingContext ctx) {
        if (null != this.biParser())
            return this.biParser().apply(ctx.type(), ctx);
        if (null != this.parser())
            return this.parser().apply(ctx);
        return Exceptional.empty();
    }
}
