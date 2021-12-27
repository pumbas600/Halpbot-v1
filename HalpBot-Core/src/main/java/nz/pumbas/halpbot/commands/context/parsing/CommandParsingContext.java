package nz.pumbas.halpbot.commands.context.parsing;

import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.context.TokenActionContext;
import nz.pumbas.halpbot.commands.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;

public interface CommandParsingContext
{

    default Exceptional<Object[]> parameters(CommandInvocationContext actionContext,
                                             ActionInvokable<CommandInvocationContext> invokable)
    {
        if (invokable instanceof TokenActionContext tokenInvokable)
            return this.parameters(actionContext, tokenInvokable);
        return Exceptional.of(new UnsupportedOperationException(
                "CommandParsingContext must be used with a class that implements TokenInvokable"));

    }

    Exceptional<Object[]> parameters(CommandInvocationContext actionContext, TokenActionContext tokenInvokable);

    Exceptional<Object> parseToken(CommandInvocationContext invocationContext,
                                   TokenActionContext invokable,
                                   Token token);
}
