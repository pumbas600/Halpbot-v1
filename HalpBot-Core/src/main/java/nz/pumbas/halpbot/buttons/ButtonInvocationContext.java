package nz.pumbas.halpbot.buttons;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;

public interface ButtonInvocationContext extends InvocationContext
{
    List<ParsingToken> nonCommandParameterTokens();

    Object[] passedParameters();

    @Override
    default String contextString() {
        return Arrays.stream(this.passedParameters())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }
}
