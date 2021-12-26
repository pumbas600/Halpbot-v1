package nz.pumbas.halpbot.buttons;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

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
