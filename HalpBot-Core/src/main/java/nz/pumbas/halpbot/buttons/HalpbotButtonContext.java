package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import java.time.Duration;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;

@Binds(ButtonContext.class)
public record HalpbotButtonContext(String id,
                                   boolean isEphemeral,
                                   Duration displayDuration,
                                   ActionInvokable<ButtonInvocationContext> actionInvokable)
    implements ButtonContext
{
    @Bound
    public HalpbotButtonContext {}
}
