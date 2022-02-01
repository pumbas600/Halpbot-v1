package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;


@Getter
@ComponentBinding(ButtonContext.class)
@AllArgsConstructor(onConstructor_ = @Bound)
public class HalpbotButtonContext implements ButtonContext
{
    private final String id;
    private final boolean isEphemeral;
    private final Duration displayDuration;
    private final ActionInvokable<ButtonInvocationContext> actionInvokable;
    private final Object[] passedParameters;
    private final List<ParsingToken> nonCommandParameterTokens;
    private int remainingUses;
    private final Duration removeAfter;
    @Nullable private final AfterRemovalFunction afterRemoval;

    @Override
    public void deductUse() {
        this.remainingUses--;
    }
}
