package nz.pumbas.halpbot.actions.cooldowns;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(CooldownDecorator.class)
public class CooldownDecorator extends CommandContextDecorator
{
    private final Map<Long, CooldownTimer> cooldownTimers = new ConcurrentHashMap<>();
    private final long cooldownDurationMs;

    @Bound
    public CooldownDecorator(CommandContext commandContext, Cooldown cooldown) {
        super(commandContext);
        this.cooldownDurationMs = cooldown.unit().toMillis(cooldown.duration());
    }

    @Override
    public <R> Exceptional<R> invoke(InvocationContext invocationContext, boolean canHaveContextLeft) {
        HalpbotEvent event = invocationContext.halpbotEvent();

        if (event == null)
            return super.invoke(invocationContext, canHaveContextLeft);
        else if (this.cooldownTimers.getOrDefault(event.getUser().getIdLong(), CooldownTimer.Empty).hasFinished()) {
            Exceptional<R> result = super.invoke(invocationContext, canHaveContextLeft);
            this.cooldownTimers.put(event.getUser().getIdLong(), new CooldownTimer(this.cooldownDurationMs));
            return result;
        }

        return Exceptional.of(new ExplainedException(this.cooldownTimers.get(event.getUser().getIdLong()).getRemainingTimeEmbed()));
    }

    @Override
    public String toString() {
        return "Cooldown decorator[%s]".formatted(super.toString());
    }
}
