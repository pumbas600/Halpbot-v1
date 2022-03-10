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

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActionInvokableDecorator<C extends InvocationContext> implements ActionInvokable<C>
{
    private final ActionInvokable<C> actionInvokable;

    @Override
    public @Nullable Object instance() {
        return this.actionInvokable.instance();
    }

    @Override
    public ExecutableElementContext<?, ?> executable() {
        return this.actionInvokable.executable();
    }

    @Override
    public Exceptional<Object[]> parameters(C invocationContext) {
        return this.actionInvokable.parameters(invocationContext);
    }

    @Override
    public <R> Exceptional<R> invoke(Object... parameters) {
        return this.actionInvokable.invoke(parameters);
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        return this.actionInvokable.invoke(invocationContext);
    }
}
