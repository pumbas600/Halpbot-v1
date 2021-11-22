package nz.pumbas.halpbot.commands.commandmethods.parsing;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.CommandManager;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.exceptions.CommandException;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.PlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;

public class MessageParsingContext implements ParsingContext
{
    @Override
    public Exceptional<Object[]> parseParameters(@NotNull InvocationContext invocationContext,
                                                 @NotNull Invokable invokable,
                                                 @NotNull List<Token> tokens,
                                                 boolean canHaveContextLeft) {
        Object[] parsedTokens = new Object[invokable.executable().parameterCount()];

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
            else parsedTokens[parameterIndex++] = parameter.orNull();
        }

        if (invocationContext.hasNext() && !canHaveContextLeft)
            return Exceptional.of(new CommandException("There appears to be too many parameters for this command"));
        return Exceptional.of(parsedTokens);
    }

    @Override
    public Exceptional<Object> parseToken(@NotNull InvocationContext invocationContext,
                                                   @NotNull Invokable invokable,
                                                   @NotNull Token token) {
        if (token instanceof ParsingToken parsingToken) {
            invocationContext.update(parsingToken.parameterContext(), parsingToken.sortedAnnotations());
            int currentIndex = invocationContext.currentIndex();

            Exceptional<Object> result = CommandManager.handleReflectionSyntax(invocationContext);
            if (result.caught() || result.orNull() != IGNORE_RESULT)
                return result;

            invocationContext.currentIndex(currentIndex);

            return parsingToken.converter()
                    .apply(invocationContext)
                    .map(o -> o);
        }
        else if (token instanceof PlaceholderToken placeholderToken) {
            if (placeholderToken.matches(invocationContext)) {
                return Exceptional.of(IGNORE_RESULT);
            }
            return Exceptional.of(new CommandException("Expected the placeholder " + placeholderToken.placeholder()));
        }
        return Exceptional.of(new CommandException("Unable to parse the parameter"));
    }
}


}
