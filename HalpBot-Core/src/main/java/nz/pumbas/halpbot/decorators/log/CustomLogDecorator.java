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

package nz.pumbas.halpbot.decorators.log;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.events.HalpbotEvent;

@ComponentBinding(value = LogDecorator.class, priority = 0)
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

        // Invoke the old decorator like normal
        return super.invoke(invocationContext);
    }
}
