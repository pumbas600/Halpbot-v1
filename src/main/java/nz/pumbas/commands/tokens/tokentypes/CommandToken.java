package nz.pumbas.commands.tokens.tokentypes;

/**
 * A representation of an individual element in an {@link nz.pumbas.commands.annotations.Command}.
 */
public interface CommandToken {

    /**
     * @return If this {@link CommandToken} is optional or not
     */
    boolean isOptional();
}
