package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.tokens.ParsingToken;
import nz.pumbas.halpbot.commands.tokens.PlaceholderToken;
import nz.pumbas.halpbot.commands.tokens.Token;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class NameVariableBuilder implements UsageBuilder
{
    @Override
    public String buildUsage(ApplicationContext applicationContext, ExecutableElementContext<?> executableContext) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Token> tokens = Invokable.tokens(applicationContext, executableContext);
        List<ParameterContext<?>> parameters = executableContext.parameters();
        int parameterIndex = 0;

        for (Token token : tokens) {
            stringBuilder.append(token.isOptional() ? '[' : '<');

            if (token instanceof ParsingToken parsingToken) {
                if (parsingToken.isCommandParameter()) {
                    stringBuilder.append(
                        HalpbotUtils.variableNameToSplitLowercase(parameters.get(parameterIndex).name()));
                }
                parameterIndex++;
            }
            else if (token instanceof PlaceholderToken placeholderToken)
                stringBuilder.append(placeholderToken.getPlaceHolder());

            stringBuilder.append(token.isOptional() ? ']' : '>')
                .append(' ');
        }
        if (!stringBuilder.isEmpty())
            // Removes the ending space
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
}
