package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.actions.invokable.ActionContextDecorator;
import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.converters.tokens.Token;

public interface TokenActionContext extends ActionContextDecorator<CommandInvocationContext>
{
    List<Token> tokens();

    Set<TypeContext<?>> reflections();

    //TODO: Perhaps pass in TokenActionContext rather than each value individually
    @Override
    default <R> Exceptional<R> invoke(CommandInvocationContext invocableContext) {
        Set<TypeContext<?>> originalReflections = invocableContext.reflections();
        List<Token> originalTokens = invocableContext.tokens();
        invocableContext.reflections(this.reflections());
        invocableContext.tokens(this.tokens());
        Exceptional<R> result = ActionContextDecorator.super.invoke(invocableContext);
        invocableContext.reflections(originalReflections);
        invocableContext.tokens(originalTokens);
        return result;
    }
}
