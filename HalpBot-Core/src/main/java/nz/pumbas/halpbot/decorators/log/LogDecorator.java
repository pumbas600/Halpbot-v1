package nz.pumbas.halpbot.decorators.log;

import net.dv8tion.jda.api.entities.AbstractChannel;

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
            AbstractChannel channel = halpbotEvent.channel();
            invocationContext.applicationContext().log().debug(
                    "%s has invoked the action %s in %s with the event '%s'".formatted(
                                    halpbotEvent.user().getAsTag(),
                                    this.executable().qualifiedName(),
                                    channel != null ? channel.getName() : "Undeterminable Channel",
                                    halpbotEvent.rawEvent()));
        }
        return super.invoke(invocationContext);
    }
}
