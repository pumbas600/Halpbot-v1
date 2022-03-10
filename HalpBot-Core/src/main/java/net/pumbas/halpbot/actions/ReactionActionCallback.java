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

package net.pumbas.halpbot.actions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.pumbas.halpbot.actions.cooldowns.CooldownTimer;
import net.pumbas.halpbot.events.HalpbotEvent;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ReactionActionCallback extends AbstractActionCallback
{
    protected final String codepointEmoji;
    protected final Function<MessageReactionAddEvent, Object> callback;
    protected final boolean removeReactionIfCoolingDown;

    protected ReactionActionCallback(
        String codepointEmoji,
        Function<MessageReactionAddEvent, Object> callback,
        boolean removeReactionIfCoolingDown,
        long deleteAfterDuration, TimeUnit deleteAfterTimeUnit,
        long cooldownDuration, TimeUnit cooldownTimeUnit,
        List<String> permissions, boolean singleUse,
        long displayDuration) {
        super(deleteAfterDuration,
            deleteAfterTimeUnit,
            cooldownDuration,
            cooldownTimeUnit,
            permissions,
            singleUse,
            displayDuration);
        this.codepointEmoji = codepointEmoji;
        this.callback = callback;
        this.removeReactionIfCoolingDown = removeReactionIfCoolingDown;
    }

    public boolean removeReactionIfCoolingDown() {
        return this.removeReactionIfCoolingDown;
    }

    @Override
    public Exceptional<Object> invokeCallback(HalpbotEvent event) {
        //Use of supplier means that any exception thrown in the callback will be automatically caught.
        return Exceptional.of(() -> this.callback.apply(event.event(MessageReactionAddEvent.class)));
    }

    public String getCodepointEmoji() {
        return this.codepointEmoji;
    }

    public @Nullable CooldownTimer cooldownTimer() {
        return null;
    }


    public long cooldownDurationMs() {
        return 0;
    }
}
