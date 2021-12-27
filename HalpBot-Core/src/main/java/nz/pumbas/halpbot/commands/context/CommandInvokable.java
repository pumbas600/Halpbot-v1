package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.exceptions.CommandException;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.PlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

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
            }
            else if (parameter.orNull() != HalpbotUtils.IGNORE_RESULT)
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
        }
        else if (token instanceof PlaceholderToken placeholderToken) {
            if (placeholderToken.matches(invocationContext)) {
                return Exceptional.of(HalpbotUtils.IGNORE_RESULT);
            }
            return Exceptional.of(new CommandException("Expected the placeholder " + placeholderToken.placeholder()));
        }
        return Exceptional.of(new CommandException("Unable to parse the parameter"));
    }
}
