package nz.pumbas.halpbot.decorators.log;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(LogDecorator.class)
public class LogDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    @Bound
    public LogDecorator(ActionInvokable<C> actionInvokable, Log log) {
        super(actionInvokable);
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {

        HalpbotEvent halpbotEvent = invocationContext.halpbotEvent();
        if (halpbotEvent != null) {
            invocationContext.applicationContext().log().debug(
                    "%s has invoked the action %s in %s with %s".formatted(
                                    halpbotEvent.getUser().getAsTag(),
                                    this.executable().qualifiedName(),
                                    halpbotEvent.getGuild().getName(),
                                    invocationContext.contextString()));
        }
        return super.invoke(invocationContext);
    }
}
