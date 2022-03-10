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

import net.pumbas.halpbot.actions.exceptions.ActionException;
import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.converters.tokens.ParsingToken;

import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

public interface ButtonInvokable extends ActionInvokable<ButtonInvocationContext>
{
    @Override
    default Exceptional<Object[]> parameters(ButtonInvocationContext invocationContext) {
        final Object[] parameters = new Object[this.executable().parameterCount()];
        final Object[] passedParameters = invocationContext.passedParameters();
        final List<ParsingToken> nonCommandParameterTokens = invocationContext.nonCommandParameterTokens();

        int passedParameterIndex = 0;
        int nonCommandParameterIndex = 0;
        int parameterIndex = 0;

        while (parameterIndex < parameters.length) {
            final ParameterContext<?> parameterContext = this.executable().parameters().get(parameterIndex);
            TypeContext<?> targetType = parameterContext.type();
            invocationContext.currentType(targetType);

            // Attempt to find matching parameter
            if (passedParameterIndex < passedParameters.length && targetType.parentOf(passedParameters[passedParameterIndex].getClass()))
                parameters[parameterIndex++] = passedParameters[passedParameterIndex++];
            else if (nonCommandParameterIndex < nonCommandParameterTokens.size()) {
                ParsingToken token = nonCommandParameterTokens.get(nonCommandParameterIndex++);
                if (token.parameterContext().equals(parameterContext)) {
                    parameters[parameterIndex++] = token.converter().apply(invocationContext).orNull();
                }
            } else return Exceptional.of(
                new ActionException("There didn't appear to be a value for the parameter %s in the button %s"
                    .formatted(targetType.qualifiedName(), this.executable().qualifiedName())));
        }
        return Exceptional.of(parameters);
    }
}
