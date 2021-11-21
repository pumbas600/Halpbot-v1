package nz.pumbas.halpbot.commands.commandmethods.parsing;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@SuppressWarnings("ConstantDeclaredInInterface")
public interface ParsingContext
{
    Object IGNORE_RESULT = new Object();

    @NotNull
    Exceptional<Object[]> parseParameters(@NotNull InvocationContext invocationContext,
                                          @NotNull Invokable invokable,
                                          @NotNull List<Token> tokens,
                                          boolean canHaveContextLeft);

    @NotNull
    Exceptional<Object> parseToken(@NotNull InvocationContext invocationContext,
                                   @NotNull Invokable invokable,
                                   @NotNull Token token);
}
