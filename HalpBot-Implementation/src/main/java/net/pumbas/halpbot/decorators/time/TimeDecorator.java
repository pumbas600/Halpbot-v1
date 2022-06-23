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

package net.pumbas.halpbot.decorators.time;

import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import net.pumbas.halpbot.actions.invokable.InvocationContext;
import net.pumbas.halpbot.utilities.LogLevel;

import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.util.Result;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class TimeDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C> {

    private final LogLevel logLevel;

    // Its important that you're @Bound constructor contains the ActionInvokable and the annotation so that
    // it can be used by the constructor. If this doesn't match, an exception will be thrown during startup
    @Bound
    public TimeDecorator(final ActionInvokable<C> actionInvokable, final Time time) {
        super(actionInvokable);
        this.logLevel = time.value();
    }

    @Override
    public <R> Result<R> invoke(final C invocationContext) {
        final OffsetDateTime start = OffsetDateTime.now();
        final Result<R> result = super.invoke(invocationContext);

        // Measure the time in milliseconds between now and before the action was invoked to see how long it took
        final double ms = start.until(OffsetDateTime.now(), ChronoUnit.NANOS) / 1_000_000D;
        this.logLevel.log(invocationContext.applicationContext(), "Invoked %s %s in %.5fms"
            .formatted(this.executable().qualifiedName(), result.caught() ? "Unsuccessfully" : "Successfully", ms));

        return result;
    }
}
