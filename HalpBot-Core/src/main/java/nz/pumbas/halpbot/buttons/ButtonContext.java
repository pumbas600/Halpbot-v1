package nz.pumbas.halpbot.buttons;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.SourceContext;

public interface ButtonContext extends SourceContext<ButtonInvocationContext>, DisplayableResult
{
    String id();

    Object[] passedParameters();
}
