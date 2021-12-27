package nz.pumbas.halpbot.decorators.log;

import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Guild;

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
    private final LogLevel logLevel;

    @Bound
    public LogDecorator(ActionInvokable<C> actionInvokable, Log log) {
        super(actionInvokable);
        this.logLevel = log.value();
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {

        HalpbotEvent halpbotEvent = invocationContext.halpbotEvent();
        if (halpbotEvent != null) {
            AbstractChannel channel = halpbotEvent.channel();
            Guild guild = halpbotEvent.guild();

            this.logLevel.log(invocationContext.applicationContext().log(),
                    "[%s][%s] %s has invoked the action %s".formatted(
                                    guild != null ? guild.getName() : "PM",
                                    channel != null ? channel.getName() : "?",
                                    halpbotEvent.user().getAsTag(),
                                    this.executable().qualifiedName()));
        }
        return super.invoke(invocationContext);
    }
}
