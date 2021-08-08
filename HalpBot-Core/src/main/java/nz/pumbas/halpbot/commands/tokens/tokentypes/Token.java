package nz.pumbas.halpbot.commands.tokens.tokentypes;

import nz.pumbas.halpbot.commands.annotations.Command;

/**
 * A representation of an individual element in an {@link Command}.
 */
public interface Token
{

    /**
     * @return If this {@link Token} is optional or not
     */
    boolean isOptional();
}
