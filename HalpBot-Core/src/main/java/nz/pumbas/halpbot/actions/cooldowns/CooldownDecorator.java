package nz.pumbas.halpbot.actions.cooldowns;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.Duration;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.common.UndisplayedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@ComponentBinding(CooldownDecorator.class)
public class CooldownDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    private static final long SECONDS_BETWEEN_COOLDOWN_EMBEDS = 15;

    private final CooldownStrategy strategy;
    private final Duration cooldownDuration;

    @Bound
    public CooldownDecorator(ActionInvokable<C> actionInvokable, Cooldown cooldown) {
        super(actionInvokable);
        this.cooldownDuration = Duration.of(cooldown.duration().value(), cooldown.duration().unit());
        this.strategy = cooldown.type().strategy();
    }

    @Override
    public <R> Exceptional<R> invoke(C invocableContext) {
        HalpbotEvent event = invocableContext.halpbotEvent();
        Guild guild = event.guild();

        long guildId = guild == null ? -1 : guild.getIdLong();
        long userId = event.user().getIdLong();

        CooldownTimer cooldownTimer = this.strategy.get(guildId, userId);

        if (cooldownTimer.hasFinished()) {
            Exceptional<R> result = super.invoke(invocableContext);
            this.strategy.put(guildId, userId, new CooldownTimer(this.cooldownDuration));
            return result;
        }

        if (cooldownTimer.canSendEmbed(SECONDS_BETWEEN_COOLDOWN_EMBEDS))
            return Exceptional.of(new ExplainedException(this.strategy.get(guildId, userId)
                    .remainingTimeEmbed(this.strategy.message())));
        if (event.rawEvent() instanceof MessageReceivedEvent messageReceivedEvent)
            // Acknowledge the request with a :stopwatch: reaction
            messageReceivedEvent.getMessage().addReaction("\u23F1").queue();
        return Exceptional.of(new UndisplayedException(this.strategy.message()));
    }
}
