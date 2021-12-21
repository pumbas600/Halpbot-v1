package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;

public interface TokenInvokable extends Invokable
{
    ParsingContext parsingContext();

    List<Token> tokens();

    default <R> Exceptional<R> invoke(InvocationContext invocationContext) {
        return this.invoke(invocationContext, false);
    }

    default <R> Exceptional<R> invoke(InvocationContext invocationContext, boolean canHaveContextLeft) {
        final Exceptional<Object[]> parameters = this.parsingContext().parseParameters(
                invocationContext,
                this,
                this.tokens(),
                canHaveContextLeft);

        if (parameters.caught())
            return Exceptional.of(parameters.error());

        return this.invoke(parameters.or(new Object[0]));
    }
}
