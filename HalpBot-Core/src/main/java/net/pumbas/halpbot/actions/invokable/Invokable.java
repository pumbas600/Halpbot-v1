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
import org.dockbox.hartshorn.util.reflect.ConstructorContext;
import org.dockbox.hartshorn.util.reflect.ExecutableElementContext;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.jetbrains.annotations.Nullable;

public interface Invokable {

    @SuppressWarnings("unchecked")
    default <R> Result<R> invoke(final Object... parameters) {
        final ExecutableElementContext<?, ?> executable = this.executable();
        if (executable instanceof MethodContext methodContext) {
            return methodContext.invoke(this.instance(), parameters);
        }
        final ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(parameters);
    }

    ExecutableElementContext<?, ?> executable();

    @Nullable
    Object instance();

    @SuppressWarnings("unchecked")
    default <R> Result<R> invoke(final ApplicationContext applicationContext) {
        final ExecutableElementContext<?, ?> executable = this.executable();
        if (executable instanceof MethodContext methodContext) {
            return methodContext.invoke(applicationContext, this.instance());
        }
        final ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(applicationContext);
    }
}
