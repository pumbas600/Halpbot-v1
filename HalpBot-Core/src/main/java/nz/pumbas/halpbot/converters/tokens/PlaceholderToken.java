package nz.pumbas.halpbot.converters.tokens;

import nz.pumbas.halpbot.commands.context.InvocationContext;

public interface PlaceholderToken extends Token
{
    String placeholder();

    boolean matches(InvocationContext invocationContext);
}
