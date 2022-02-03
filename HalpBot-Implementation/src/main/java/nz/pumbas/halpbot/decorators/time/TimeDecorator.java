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

package nz.pumbas.halpbot.decorators.time;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.utilities.LogLevel;

// Bind the TimeDecorator to this instance so that the factory knows to instantiate this
@ComponentBinding(TimeDecorator.class)
public class TimeDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    private final LogLevel logLevel;

    // Its important that you're @Bound constructor contains the ActionInvokable and the annotation so that
    // it can be used by the constructor. If this doesn't match, an exception will be thrown during startup
    @Bound
    public TimeDecorator(ActionInvokable<C> actionInvokable, Time time) {
        super(actionInvokable);
        this.logLevel = time.value();
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        OffsetDateTime start = OffsetDateTime.now();
        Exceptional<R> result = super.invoke(invocationContext);

        // Measure the time in milliseconds between now and before the action was invoked to see how long it took
        double ms = start.until(OffsetDateTime.now(), ChronoUnit.NANOS) / 1_000_000D;
        this.logLevel.log(invocationContext.applicationContext(), "Invoked %s %s in %.5fms"
                .formatted(this.executable().qualifiedName(), result.caught() ? "Unsuccessfully" : "Successfully", ms));

        return result;
    }
}
