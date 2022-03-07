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

package nz.pumbas.halpbot.actions;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.actions.cooldowns.CooldownTimer;
import nz.pumbas.halpbot.events.HalpbotEvent;

public class ButtonActionCallback extends AbstractActionCallback
{
    private final Method callback;
    private final Object instance;
    private final boolean isEphemeral;
    private final Object[] parameters;

    protected ButtonActionCallback(
        Method callback, Object instance,
        boolean isEphemeral, Object[] parameters,
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

        this.callback = callback;
        this.instance = instance;
        this.isEphemeral = isEphemeral;
        this.parameters = parameters;
    }

    @Override
    public Exceptional<Object> invokeCallback(HalpbotEvent event) {
        return Exceptional.empty();
    }

    public Method getMethod() {
        return this.callback;
    }

    public @Nullable Object getInstance() {
        return this.instance;
    }

    public boolean isEphemeral() {
        return this.isEphemeral;
    }

    public ActionCallbackBuilder copy() {
        return new ActionCallbackBuilder()
            .setButtonAction(this.instance, this.callback)
            .setParameters(this.parameters);
    }

    public @Nullable CooldownTimer cooldownTimer() {
        return null;
    }

    public long cooldownDurationMs() {
        return 0;
    }
}
