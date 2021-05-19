package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link ParsingToken Parsing tokens} are tokens which have a specific type and can parse an inputted {@link String} to this type.
 */
public interface ParsingToken extends CommandToken {

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

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Nullable
    Object getDefaultValue();

    /**
     * Parses the {@link String default value} for a token. If this {@link String default value} is null, then it returns null.
     * 
     * @param defaultValue
     *      {@link String default value} to be parsed into an {@link Object} using {@link ParsingToken#parse(String)}
     *      
     * @return The {@link Object} parsed default value
     */
    @Nullable
    default Object parseDefaultValue(@Nullable String defaultValue) {
        if (null == defaultValue)
            return null;
        else {
            if (!this.matches(defaultValue))
                throw new IllegalArgumentException(
                        String.format("The default value %s doesn't match the required format of the type %s", defaultValue, this.getType().getSimpleName()));
            return this.parse(defaultValue);
        }
    }
}
