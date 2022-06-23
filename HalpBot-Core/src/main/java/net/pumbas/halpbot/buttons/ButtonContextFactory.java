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

package net.pumbas.halpbot.buttons;

import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.objects.AsyncDuration;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.factory.Factory;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

@Service
@RequiresActivator(UseButtons.class)
public interface ButtonContextFactory {

    default ButtonContext create(final String id,
                                 final Object[] passedParameters,
                                 final ButtonContext buttonContext,
                                 @Nullable final AfterRemovalFunction afterRemoval)
    {
        return this.create(
            id,
            buttonContext.isEphemeral(),
            buttonContext.displayDuration(),
            buttonContext.actionInvokable(),
            passedParameters,
            buttonContext.nonCommandParameterTokens(),
            buttonContext.removeAfter(),
            afterRemoval,
            buttonContext.remainingUses());
    }

    @Factory
    ButtonContext create(String id,
                         boolean isEphemeral,
                         Duration displayDuration,
                         ActionInvokable<ButtonInvocationContext> actionInvokable,
                         Object[] passedParameters,
                         List<ParsingToken> nonCommandParameterTokens,
                         AsyncDuration removeAfter,
                         @Nullable AfterRemovalFunction afterRemoval,
                         int remainingUses);
}
