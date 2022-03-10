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

package net.pumbas.halpbot.commands.actioninvokable;

import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import net.pumbas.halpbot.commands.exceptions.CommandException;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.converters.tokens.PlaceholderToken;
import net.pumbas.halpbot.converters.tokens.Token;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

public interface CommandInvokable extends ActionInvokable<CommandInvocationContext>
{
    @Override
    default Exceptional<Object[]> parameters(CommandInvocationContext invocationContext) {
        final List<Token> tokens = invocationContext.tokens();
        final Object[] parsedTokens = new Object[this.executable().parameterCount()];

        int tokenIndex = 0;
        int parameterIndex = 0;

        while (parameterIndex < parsedTokens.length) {
            if (tokenIndex >= tokens.size())
                return Exceptional.of(new CommandException("There appears to be too many parameters for this command"));

            Token currentToken = tokens.get(tokenIndex++);

            Exceptional<Object> parameter = this.parseToken(invocationContext, currentToken);
            if (parameter.caught()) {
                if (!currentToken.isOptional())
                    return Exceptional.of(parameter.error());

                if (currentToken instanceof ParsingToken parsingToken)
                    parsedTokens[parameterIndex++] = parsingToken.defaultValue();
            } else if (parameter.orNull() != HalpbotUtils.IGNORE_RESULT)
                parsedTokens[parameterIndex++] = parameter.orNull();
        }

        if (invocationContext.hasNext() && !invocationContext.canHaveContextLeft())
            return Exceptional.of(new CommandException("There appears to be too many parameters for this command"));
        return Exceptional.of(parsedTokens);
    }

    default Exceptional<Object> parseToken(CommandInvocationContext invocationContext, Token token) {
        if (token instanceof ParsingToken parsingToken) {
            invocationContext.update(parsingToken.parameterContext(), parsingToken.sortedAnnotations());
            return parsingToken.converter()
                .apply(invocationContext)
                .map(o -> o);
        } else if (token instanceof PlaceholderToken placeholderToken) {
            if (placeholderToken.matches(invocationContext)) {
                return Exceptional.of(HalpbotUtils.IGNORE_RESULT);
            }
            return Exceptional.of(new CommandException("Expected the placeholder " + placeholderToken.placeholder()));
        }
        return Exceptional.of(new CommandException("Unable to parse the parameter"));
    }
}
