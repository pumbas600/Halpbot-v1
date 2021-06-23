package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.objects.Result;

/**
 * {@link ParsingToken Parsing tokens} are tokens which have a specific type and can parse an inputted {@link String} to this type.
 */
public interface ParsingToken extends CommandToken {

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    Annotation[] getAnnotations();

    /**
     * @return The required {@link Class type} of this {@link ParsingToken}
     */
    Class<?> getType();

    /**
     * Parses an {@link InvocationTokenInfo invocation token} to the type of the {@link ParsingToken}.
     *
     * @param invocationToken
     *      The {@link InvocationTokenInfo invocation token} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} parsing the {@link InvocationTokenInfo invocation token} to the correct type
     */
    Object parse(@NotNull InvocationTokenInfo invocationToken);

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Nullable
    Object getDefaultValue();

    /**
     * Parses the {@link String default value} for a token. If this {@link String default value} is null or a string of 'null',
     * then it returns null.
     * 
     * @param defaultValue
     *      {@link String default value} to be parsed into an {@link Object} using {@link ParsingToken#parse(InvocationTokenInfo)}
     *      
     * @return The parsed {@link Object default value}
     */
    @Nullable
    default Object parseDefaultValue(@Nullable String defaultValue) {
        if (null == defaultValue || "null".equalsIgnoreCase(defaultValue))
            return null;
        else {
            InvocationTokenInfo invocationToken = InvocationTokenInfo.of(defaultValue).saveState(this);
            if (!this.matches(invocationToken))
                throw new IllegalArgumentException(
                        String.format("The default value %s doesn't match the required format of the type %s", defaultValue, this.getType().getSimpleName()));

            return this.parse(invocationToken.restoreState(this));
        }
    }
}
