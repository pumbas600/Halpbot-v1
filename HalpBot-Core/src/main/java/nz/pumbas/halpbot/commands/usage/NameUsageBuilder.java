package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.HalpbotPlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class NameUsageBuilder implements UsageBuilder
{
    @Override
    public String buildUsage(ApplicationContext applicationContext, ExecutableElementContext<?> executableContext) {
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
