package nz.pumbas.halpbot.buttons;

import nz.pumbas.halpbot.actions.invokable.SourceInvocationContext;

public interface ButtonInvocationContext extends SourceInvocationContext
{
    Object[] passedParameters();
}
