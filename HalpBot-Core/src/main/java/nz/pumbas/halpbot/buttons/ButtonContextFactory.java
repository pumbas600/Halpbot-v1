package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.time.Duration;
import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;

@Service
public interface ButtonContextFactory
{
    @Factory
    ButtonContext create(String id,
                         boolean isEphemeral,
                         Duration displayDuration,
                         ActionInvokable<ButtonInvocationContext> actionInvokable,
                         Object[] passedParameters,
                         List<ParsingToken> nonCommandParameterTokens,
                         int afterUsages,
                         Duration after);

    default ButtonContext create(String id, Object[] passedParameters, ButtonContext buttonContext) {
        return this.create(
                id,
                buttonContext.isEphemeral(),
                buttonContext.displayDuration(),
                buttonContext.actionInvokable(),
                passedParameters,
                buttonContext.nonCommandParameterTokens(),
                buttonContext.afterUsages(),
                buttonContext.after());
    }
}
