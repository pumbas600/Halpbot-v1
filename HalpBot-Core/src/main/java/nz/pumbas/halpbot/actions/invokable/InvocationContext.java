package nz.pumbas.halpbot.actions.invokable;

import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.events.HalpbotEvent;

public interface InvocationContext
{
    @Nullable
    HalpbotEvent halpbotEvent();
}
