package nz.pumbas.halpbot.actions.invokable;

import java.util.List;

import nz.pumbas.halpbot.converters.tokens.ParsingToken;

public interface SourceInvocationContext extends InvocationContext
{
    List<ParsingToken> nonCommandParameterTokens();
}
