package nz.pumbas.halpbot.actions.cooldowns;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
import nz.pumbas.halpbot.common.UndisplayedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(CooldownDecorator.class)
public class CooldownDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    private static final long SECONDS_BETWEEN_COOLDOWN_EMBEDS = 15;

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
        CooldownTimer cooldownTimer = this.cooldownTimers.getOrDefault(event.user().getIdLong(), CooldownTimer.Empty);

        if (cooldownTimer.hasFinished()) {
            Exceptional<R> result = super.invoke(invocableContext);
            this.cooldownTimers.put(event.user().getIdLong(), new CooldownTimer(this.cooldownDuration));
            return result;
        }

        if (cooldownTimer.canSendEmbed(SECONDS_BETWEEN_COOLDOWN_EMBEDS))
            return Exceptional.of(new ExplainedException(this.cooldownTimers.get(event.user().getIdLong()).remainingTimeEmbed()));
        if (event.rawEvent() instanceof MessageReceivedEvent messageReceivedEvent)
            messageReceivedEvent.getMessage().addReaction("\u23F1").queue();
        return Exceptional.of(new UndisplayedException("You're currently on cooldown"));
    }
}
