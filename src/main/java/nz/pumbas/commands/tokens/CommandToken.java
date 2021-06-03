package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

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
}
