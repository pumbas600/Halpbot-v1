/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.actions.cooldowns;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import net.pumbas.halpbot.actions.invokable.InvocationContext;
import net.pumbas.halpbot.common.ExplainedException;
import net.pumbas.halpbot.common.UndisplayedException;
import net.pumbas.halpbot.events.HalpbotEvent;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.Duration;

@ComponentBinding(CooldownDecorator.class)
public class CooldownDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    private static final long SECONDS_BETWEEN_COOLDOWN_EMBEDS = 15;

    private final CooldownStrategy strategy;
    private final Duration cooldownDuration;

    @Bound
    public CooldownDecorator(ActionInvokable<C> actionInvokable, Cooldown cooldown) {
        super(actionInvokable);
        this.cooldownDuration = HalpbotUtils.asDuration(cooldown.duration());
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
