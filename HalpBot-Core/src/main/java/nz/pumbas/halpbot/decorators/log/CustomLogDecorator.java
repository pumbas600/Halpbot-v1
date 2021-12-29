package nz.pumbas.halpbot.decorators.log;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(value = LogDecorator.class, priority = 0)
public class CustomLogDecorator<C extends InvocationContext> extends LogDecorator<C>
{
    @Bound
    public CustomLogDecorator(ActionInvokable<C> actionInvokable, Log log) {
        super(actionInvokable, log);
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        HalpbotEvent halpbotEvent = invocationContext.halpbotEvent();
        if (halpbotEvent.rawEvent() instanceof MessageReceivedEvent messageEvent) {
            this.logLevel().log(invocationContext.applicationContext(),
                    "[%s] %s".formatted(messageEvent.getClass().getSimpleName(), messageEvent.getMessage().getContentRaw()));
        }

        // Invoke the old decorator like normally
        return super.invoke(invocationContext);
    }
}
