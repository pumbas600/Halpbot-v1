package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.domain.Exceptional;

public interface ActionParsingContext<C extends ActionContext>
{
    Exceptional<Object[]> parameters(C actionContext,
                                     ActionInvokable<?, C> invokable);
}
