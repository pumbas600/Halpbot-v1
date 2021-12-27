package nz.pumbas.halpbot.converters.tokens;

import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;

public interface PlaceholderToken extends Token
{
    String placeholder();

    boolean matches(CommandInvocationContext invocationContext);
}
