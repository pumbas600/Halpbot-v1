package nz.pumbas.halpbot.commands.context.parsing;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import nz.pumbas.halpbot.commands.TokenInvokable;
import nz.pumbas.halpbot.commands.context.CommandInvocationContext;
import nz.pumbas.halpbot.commands.exceptions.CommandException;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.PlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class MessageCommandParsingContext implements CommandParsingContext
{
    @Override
    public Exceptional<Object[]> parameters(CommandInvocationContext invocationContext,
                                            TokenInvokable invokable)
    {
        final List<Token> tokens = invokable.tokens();
        final Object[] parsedTokens = new Object[invokable.executable().parameterCount()];

        int tokenIndex = 0;
        int parameterIndex = 0;

        while (parameterIndex < parsedTokens.length) {
            if (tokenIndex >= tokens.size())
                return Exceptional.of(new CommandException("There appears to be too many parameters for this command"));

            Token currentToken = tokens.get(tokenIndex++);

            Exceptional<Object> parameter = this.parseToken(
                    invocationContext,
                    invokable,
                    currentToken);

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

    @Override
    public Exceptional<Object> parseToken(CommandInvocationContext invocationContext,
                                          TokenInvokable invokable,
                                          Token token)
    {
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
