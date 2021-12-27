package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.time.Duration;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;

@Service
public interface ButtonContextFactory
{
    @Factory
    ButtonContext create(String id,
                         boolean isEphemeral,
                         Duration displayDuration,
                         ActionInvokable<ButtonInvocationContext> actionInvokable);
}
