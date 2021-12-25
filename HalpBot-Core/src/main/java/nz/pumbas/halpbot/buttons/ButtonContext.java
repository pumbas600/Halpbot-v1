package nz.pumbas.halpbot.buttons;

import nz.pumbas.halpbot.actions.methods.Invokable;

public interface ButtonContext extends Invokable
{
    String id();

    boolean isEphemeral();
}
