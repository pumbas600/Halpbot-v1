package nz.pumbas.halpbot.buttons;

import java.time.Duration;

import nz.pumbas.halpbot.actions.methods.Invokable;

public interface ButtonContext extends Invokable
{
    String id();

    boolean isEphemeral();

    Duration displayDuration();
}
