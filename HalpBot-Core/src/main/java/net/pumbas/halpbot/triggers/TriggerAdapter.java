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

package net.pumbas.halpbot.triggers;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.adapters.HalpbotAdapter;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.List;

@Service
public interface TriggerAdapter extends HalpbotAdapter {

    @Override
    default void onEvent(final GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent)
            this.onMessageReceived(messageReceivedEvent);
    }

    void onMessageReceived(MessageReceivedEvent event);

    default <T> void registerTriggers(final TypeContext<T> type) {
        final T instance = this.applicationContext().get(type);
        int triggers = 0;

        for (final MethodContext<?, T> trigger : type.methods(Trigger.class)) {
            if (!trigger.isPublic()) {
                this.applicationContext().log().warn("Methods annotated with @Trigger must be public");
                continue;
            }
            this.registerTrigger(instance, trigger);
            triggers++;
        }
        this.applicationContext().log().info("Registered %d triggers found in %s".formatted(triggers, type.qualifiedName()));
    }

    <T> void registerTrigger(T instance, MethodContext<?, T> methodContext);

    List<TriggerContext> triggerContexts();
}
