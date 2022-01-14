package nz.pumbas.halpbot.actions.invokable;

import java.util.List;

import nz.pumbas.halpbot.converters.tokens.ParsingToken;

public interface SourceContext<C extends SourceInvocationContext> extends ActionContextDecorator<C>
{
    List<ParsingToken> nonCommandParameterTokens();
}
