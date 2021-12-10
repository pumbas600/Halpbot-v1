package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.PlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.Reflect;

public class TypeUsageBuilder implements UsageBuilder
{
    @Override
    public String buildUsage(ApplicationContext applicationContext, ExecutableElementContext<?> executableContext) {
        StringBuilder stringBuilder = new StringBuilder();
        List<ParameterContext<?>> parameters = executableContext.parameters();
        int parameterIndex = 0;

        List<Token> tokens = Invokable.tokens(applicationContext, executableContext);
        for (Token token : tokens) {
            if (token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter()) {
                parameterIndex++;
                continue;
            }

            stringBuilder.append(token.isOptional() ? '[' : '<');

            if (token instanceof ParsingToken) {
                Class<?> type = Reflect.wrapPrimative(parameters.get(parameterIndex++).type());
                stringBuilder.append(HalpbotUtils.splitVariableName(type.getSimpleName()));
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
