package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.converters.tokens.TokenService;

public interface TokenInvokable extends Invokable
{
    ParsingContext parsingContext();

    default List<Token> tokens(ApplicationContext applicationContext) {
        return applicationContext.get(TokenService.class).tokens(this.executable());
    }

    default <R> Exceptional<R> invoke(InvocationContext invocationContext) {
        return this.invoke(invocationContext, false);
    }

    default <R> Exceptional<R> invoke(InvocationContext invocationContext, boolean canHaveContextLeft) {
        final List<Token> tokens = this.tokens(invocationContext.applicationContext());
        final Exceptional<Object[]> parameters = this.parsingContext().parseParameters(
                invocationContext,
                this,
                tokens,
                canHaveContextLeft);

        if (parameters.caught())
            return Exceptional.of(parameters.error());

        return this.invoke(parameters.or(new Object[0]));
    }
}
