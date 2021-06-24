package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.tokens.tokensyntax.InvocationContext;

/**
 * A representation of an individual element in an {@link nz.pumbas.commands.annotations.Command}.
 */
public interface CommandToken {

    /**
     * @return If this {@link CommandToken} is optional or not
     */
    boolean isOptional();

    /**
     * Returns if the passed in @link InvocationTokenInfo invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *      The {@link InvocationContext invocation token} containing the invoking information
     *
     * @return If the {@link InvocationContext invocation token} matches this {@link CommandToken}
     */
    boolean matchesOld(@NotNull InvocationContext invocationToken);
}
