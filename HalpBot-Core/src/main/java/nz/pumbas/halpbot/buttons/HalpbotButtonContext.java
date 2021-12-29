package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import java.time.Duration;
import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.Token;

@Binds(ButtonContext.class)
public record HalpbotButtonContext(String id,
                                   boolean isEphemeral,
                                   Duration displayDuration,
                                   ActionInvokable<ButtonInvocationContext> actionInvokable,
                                   Object[] passedParameters,
                                   List<ParsingToken> nonCommandParameterTokens)
    implements ButtonContext
{
    @Bound
    public HalpbotButtonContext {}
}
