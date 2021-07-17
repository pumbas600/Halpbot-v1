package nz.pumbas.commands.tokens.tokentypes;

/**
 * A representation of an individual element in an {@link nz.pumbas.commands.annotations.Command}.
 */
public interface Token
{

    /**
     * @return If this {@link Token} is optional or not
     */
    boolean isOptional();
}
