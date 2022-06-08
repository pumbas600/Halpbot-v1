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

package net.pumbas.halpbot.converters.tokens;

import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;

import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.inject.binding.ComponentBinding;

/**
 * A placeholder token. These are usually when you add flavouring text in commands. For example, in the command: {@code
 * <my name is> #String}, the text 'my name is' are {@link HalpbotPlaceholderToken placeholder tokens}.
 */
@ComponentBinding(PlaceholderToken.class)
public record HalpbotPlaceholderToken(boolean isOptional, String placeholder)
    implements PlaceholderToken
{
    @Bound
    public HalpbotPlaceholderToken {}

    /**
     * Returns if the passed in {@link CommandInvocationContext context} matches this {@link Token}.
     *
     * @param invocationContext
     *     The {@link CommandInvocationContext context}
     *
     * @return If the {@link CommandInvocationContext context} matches this {@link Token}
     */
    @Override
    public boolean matches(CommandInvocationContext invocationContext) {
        return invocationContext.nextMatches(this.placeholder());
    }

    /**
     * @return A {@link String} representation of this token in the format {@code PlaceholderToken{isOptional=%s,
     *     placeholder=%s}}
     */
    @Override
    public String toString() {
        return "PlaceholderToken{isOptional=%s, placeholder=%s}".formatted(this.isOptional, this.placeholder());
    }
}
