package nz.pumbas.halpbot.buttons;

import java.time.Duration;
import java.util.List;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.ActionContextDecorator;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.Token;

public interface ButtonContext extends ActionContextDecorator<ButtonInvocationContext>, DisplayableResult
{
    String id();

    Object[] passedParameters();

    List<ParsingToken> nonCommandParameterTokens();
}
