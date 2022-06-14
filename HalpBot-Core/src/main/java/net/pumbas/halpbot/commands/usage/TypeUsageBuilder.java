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

package net.pumbas.halpbot.commands.usage;

import net.pumbas.halpbot.converters.tokens.HalpbotPlaceholderToken;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.converters.tokens.Token;
import net.pumbas.halpbot.converters.tokens.TokenService;
import net.pumbas.halpbot.utilities.HalpbotUtils;
import net.pumbas.halpbot.utilities.Reflect;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.ExecutableElementContext;
import org.dockbox.hartshorn.util.reflect.ParameterContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.List;

public class TypeUsageBuilder implements UsageBuilder {

    @Override
    public String buildUsage(final ApplicationContext applicationContext, final ExecutableElementContext<?, ?> executableContext) {
        final TokenService tokenService = applicationContext.get(TokenService.class);
        final StringBuilder stringBuilder = new StringBuilder();
        final List<ParameterContext<?>> parameters = executableContext.parameters();
        int parameterIndex = 0;

        final List<Token> tokens = tokenService.tokens(executableContext);
        for (final Token token : tokens) {
            if (token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter()) {
                parameterIndex++;
                continue;
            }

            stringBuilder.append(token.isOptional() ? '[' : '<');

            if (token instanceof ParsingToken) {
                final TypeContext<?> type = Reflect.wrapPrimative(parameters.get(parameterIndex++).type());
                stringBuilder.append(HalpbotUtils.splitVariableName(type.name()));
            }
            else if (token instanceof HalpbotPlaceholderToken placeholderToken)
                stringBuilder.append(placeholderToken.placeholder());

            stringBuilder.append(token.isOptional() ? ']' : '>')
                .append(' ');
        }
        if (!stringBuilder.isEmpty())
            // Removes the ending space
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
