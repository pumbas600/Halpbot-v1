package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.PlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class VariableNameBuilder implements UsageBuilder
{
    @Override
    public String buildUsage(ApplicationContext applicationContext, ExecutableElementContext<?> executableContext) {
        StringBuilder stringBuilder = new StringBuilder();
        Deque<ParameterContext<?>> parameters = executableContext.parameters();

        List<Token> tokens = Invokable.tokens(applicationContext, executableContext);
        for (Token token : tokens) {
            if (token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter()) {
                parameters.removeFirst();
                continue;
            }

            stringBuilder.append(token.isOptional() ? '[' : '<');

            if (token instanceof ParsingToken) {
                ParameterContext<?> parameterContext = parameters.removeFirst();
                stringBuilder.append(HalpbotUtils.variableNameToSplitLowercase(parameterContext.name()));
            }

            else if (token instanceof PlaceholderToken placeholderToken)
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
