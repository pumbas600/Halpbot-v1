package nz.pumbas.halpbot.commands.context.parsing;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionContext;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionParsingContext;
import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.TokenInvokable;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@SuppressWarnings("ConstantDeclaredInInterface")
public interface CommandParsingContext extends ActionParsingContext<InvocationContext>
{
    Object IGNORE_RESULT = new Object();

    @Override
    default Exceptional<Object[]> parameters(InvocationContext actionContext,
                                             ActionInvokable<?, InvocationContext> invokable)
    {
        if (invokable instanceof TokenInvokable tokenInvokable)
            return this.parameters(actionContext, tokenInvokable);
        return Exceptional.of(new UnsupportedOperationException(
                "CommandParsingContext must be used with a class that implements TokenInvokable"));

    }

    Exceptional<Object[]> parameters(InvocationContext actionContext, TokenInvokable tokenInvokable);

    Exceptional<Object> parseToken(InvocationContext invocationContext,
                                   TokenInvokable invokable,
                                   Token token);
}
