package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

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
