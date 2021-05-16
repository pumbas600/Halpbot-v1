package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

/**
 * {@link ParsingToken Parsing tokens} are tokens which have a specific type and can parse an inputted {@link String} to this type.
 */
public interface ParsingToken {

    /**
     * @return The required {@link Class type} of this {@link ParsingToken}
     */
    Class<?> getType();

    /**
     * Parses an {@link String invocation token} to the type of the {@link ParsingToken}.
     *
     * @param invocationToken
     *      The {@link String} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} of the {@link String invocation token} parsed to the correct type
     */
    Object parse(@NotNull String invocationToken);
}
