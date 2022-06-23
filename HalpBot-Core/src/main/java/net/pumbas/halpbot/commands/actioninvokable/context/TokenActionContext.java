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

package net.pumbas.halpbot.commands.actioninvokable.context;

import net.pumbas.halpbot.actions.invokable.ActionContextDecorator;
import net.pumbas.halpbot.converters.tokens.Token;

import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.List;
import java.util.Set;

public interface TokenActionContext extends ActionContextDecorator<CommandInvocationContext> {

    //TODO: Perhaps pass in TokenActionContext rather than each value individually
    @Override
    default <R> Result<R> invoke(CommandInvocationContext invocableContext) {
        Set<TypeContext<?>> originalReflections = invocableContext.reflections();
        List<Token> originalTokens = invocableContext.tokens();
        invocableContext.reflections(this.reflections());
        invocableContext.tokens(this.tokens());
        Result<R> result = ActionContextDecorator.super.invoke(invocableContext);
        invocableContext.reflections(originalReflections);
        invocableContext.tokens(originalTokens);
        return result;
    }

    Set<TypeContext<?>> reflections();

    List<Token> tokens();
}
