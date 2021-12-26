package nz.pumbas.halpbot.buttons;

import java.util.List;

import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;

public interface ButtonInvocationContext extends InvocationContext
{
    List<ParsingToken> nonCommandParameterTokens();

    Object[] passedParameters();
}
