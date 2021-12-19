package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.converters.tokens.TokenService;

public interface Invokable
{
    @Nullable Object instance();

    ExecutableElementContext<?> executable();

    ParsingContext parsingContext();

    default List<Token> tokens(ApplicationContext applicationContext) {
        return applicationContext.get(TokenService.class).tokens(this.executable());
    }

    default <R> Exceptional<R> invoke(InvocationContext invocationContext) {
        return this.invoke(invocationContext, false);
    }

    @SuppressWarnings("unchecked")
    default <R> Exceptional<R> invoke(InvocationContext invocationContext, boolean canHaveContextLeft) {
        final ExecutableElementContext<?> executable = this.executable();
        final List<Token> tokens = this.tokens(invocationContext.applicationContext());
        final Exceptional<Object[]> parameters = this.parsingContext().parseParameters(
                invocationContext,
                this,
                tokens,
                canHaveContextLeft);

        if (parameters.caught())
            return Exceptional.of(parameters.error());

        if (executable instanceof MethodContext methodContext) {
            return methodContext.invoke(this.instance(), parameters.or(new Object[0]));
        }
        ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(parameters.or(new Object[0]));

    }
}
