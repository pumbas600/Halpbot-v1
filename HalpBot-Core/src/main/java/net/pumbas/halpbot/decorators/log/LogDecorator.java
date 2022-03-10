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

package net.pumbas.halpbot.decorators.log;

import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import net.pumbas.halpbot.actions.invokable.InvocationContext;
import net.pumbas.halpbot.events.HalpbotEvent;
import net.pumbas.halpbot.utilities.LogLevel;

import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.domain.Exceptional;

import lombok.Getter;

@ComponentBinding(LogDecorator.class)
public class LogDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    @Getter
    private final LogLevel logLevel;

    @Bound
    public LogDecorator(ActionInvokable<C> actionInvokable, Log log) {
        super(actionInvokable);
        this.logLevel = log.value();
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        HalpbotEvent halpbotEvent = invocationContext.halpbotEvent();
        AbstractChannel channel = halpbotEvent.channel();
        Guild guild = halpbotEvent.guild();

        this.logLevel.log(invocationContext.applicationContext(),
            "[%s][%s] %s has invoked the action %s".formatted(
                guild != null ? guild.getName() : "PM",
                channel != null ? channel.getName() : "?",
                halpbotEvent.user().getAsTag(),
                this.executable().qualifiedName()));

        return super.invoke(invocationContext);
    }
}
