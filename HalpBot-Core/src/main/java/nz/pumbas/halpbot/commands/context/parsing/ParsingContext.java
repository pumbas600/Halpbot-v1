package nz.pumbas.halpbot.commands.context.parsing;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@SuppressWarnings("ConstantDeclaredInInterface")
public interface ParsingContext
{
    Object IGNORE_RESULT = new Object();

    Exceptional<Object[]> parseParameters(InvocationContext invocationContext,
                                          Invokable invokable,
                                          List<Token> tokens,
                                          boolean canHaveContextLeft);

    Exceptional<Object> parseToken(InvocationContext invocationContext,
                                   Invokable invokable,
                                   Token token);
}
