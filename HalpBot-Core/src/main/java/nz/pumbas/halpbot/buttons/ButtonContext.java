package nz.pumbas.halpbot.buttons;

import java.time.Duration;

import nz.pumbas.halpbot.actions.invokable.ActionContextDecorator;

public interface ButtonContext extends ActionContextDecorator<ButtonInvocationContext>
{
    String id();

    boolean isEphemeral();

    Duration displayDuration();
}
