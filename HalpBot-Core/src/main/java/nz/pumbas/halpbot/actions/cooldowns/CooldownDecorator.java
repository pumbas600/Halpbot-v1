package nz.pumbas.halpbot.actions.cooldowns;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(CooldownDecorator.class)
public class CooldownDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    private final Map<Long, CooldownTimer> cooldownTimers = new ConcurrentHashMap<>();
    private final Duration cooldownDuration;

    @Bound
    public CooldownDecorator(ActionInvokable<C> actionInvokable, Cooldown cooldown) {
        super(actionInvokable);
        this.cooldownDuration = Duration.of(cooldown.duration().value(), cooldown.duration().unit());
    }

    @Override
    public <R> Exceptional<R> invoke(C invocableContext) {
        HalpbotEvent event = invocableContext.halpbotEvent();
        if (this.cooldownTimers.getOrDefault(event.user().getIdLong(), CooldownTimer.Empty).hasFinished()) {
            Exceptional<R> result = super.invoke(invocableContext);
            this.cooldownTimers.put(event.user().getIdLong(), new CooldownTimer(this.cooldownDuration));
            return result;
        }

        return Exceptional.of(new ExplainedException(this.cooldownTimers.get(event.user().getIdLong()).remainingTimeEmbed()));
    }
}
