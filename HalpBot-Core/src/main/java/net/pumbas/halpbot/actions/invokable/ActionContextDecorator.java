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

package net.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.ExecutableElementContext;
import org.jetbrains.annotations.Nullable;

public interface ActionContextDecorator<C extends InvocationContext> extends ActionInvokable<C> {

    @Override
    default <R> Result<R> invoke(C invocationContext) {
        return this.actionInvokable().invoke(invocationContext);
    }

    @Override
    default Result<Object[]> parameters(C invocationContext) {
        return this.actionInvokable().parameters(invocationContext);
    }

    ActionInvokable<C> actionInvokable();

    @Override
    default <R> Result<R> invoke(Object... parameters) {
        return this.actionInvokable().invoke(parameters);
    }

    @Override
    default ExecutableElementContext<?, ?> executable() {
        return this.actionInvokable().executable();
    }

    @Override
    @Nullable
    default Object instance() {
        return this.actionInvokable().instance();
    }

    @Override
    default <R> Result<R> invoke(ApplicationContext applicationContext) {
        return this.actionInvokable().invoke(applicationContext);
    }
}
