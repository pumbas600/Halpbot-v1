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

package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.util.List;

import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.HalpbotPlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class NameUsageBuilder implements UsageBuilder
{
    @Override
    public boolean isValid(ApplicationContext applicationContext) {
        if ("applicationContext".equals(TypeContext.of(this)
                .method("isValid", ApplicationContext.class)
                .get()
                .parameters()
                .get(0)
                .name()))
        {
            return true;
        }
        applicationContext.log()
                .warn("""
                      Parameter names have not been preserved so a %s cannot be used. To preserve parameter names
                      add the following to your gradle.build file:
                                              
                      tasks.withType(JavaCompile) {
                            options.compilerArgs << '-parameters'
                      }
                      """.formatted(NameUsageBuilder.class.getCanonicalName()));
        return false;
    }

    @Override
    public String buildUsage(ApplicationContext applicationContext, ExecutableElementContext<?, ?> executableContext) {
        TokenService tokenService = applicationContext.get(TokenService.class);
        StringBuilder stringBuilder = new StringBuilder();
        List<ParameterContext<?>> parameters = executableContext.parameters();
        int parameterIndex = 0;

        List<Token> tokens = tokenService.tokens(executableContext);
        for (Token token : tokens) {
            if (token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter()) {
                parameterIndex++;
                continue;
            }

            stringBuilder.append(token.isOptional() ? '[' : '<');

            if (token instanceof ParsingToken) {
                ParameterContext<?> parameterContext = parameters.get(parameterIndex++);
                stringBuilder.append(HalpbotUtils.variableNameToSplitLowercase(parameterContext.name()));
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
