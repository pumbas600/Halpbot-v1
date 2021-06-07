package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;

/**
 * A representation of an individual element in an {@link nz.pumbas.commands.annotations.Command}.
 */
public interface CommandToken {

    /**
     * @return If this {@link CommandToken} is optional or not
     */
    boolean isOptional();

    /**
     * Returns if the passed in {@link String invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *      An individual element in the invocation of an {@link nz.pumbas.commands.annotations.Command}.
     *
     * @return If the {@link String invocation token} matches this {@link CommandToken}
     */
    boolean matches(@NotNull String invocationToken);

    /**
     * Returns if the passed in @link InvocationTokenInfo invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *      The {@link InvocationTokenInfo invocation token} containing the invoking information
     *
     * @return If the {@link InvocationTokenInfo invocation token} matches this {@link CommandToken}
     */
    boolean matches(@NotNull InvocationTokenInfo invocationToken);
}
