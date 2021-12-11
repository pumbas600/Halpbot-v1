package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import nz.pumbas.halpbot.commands.commandmethods.parsing.ParsingContext;
import nz.pumbas.halpbot.commands.context.InvocationContext;
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

    default Exceptional<Object[]> parseParameters(InvocationContext invocationContext, boolean canHaveContextLeft) {
        final List<Token> tokens = this.tokens(invocationContext.applicationContext());
        return this.parsingContext().parseParameters(
                invocationContext,
                this,
                tokens,
                canHaveContextLeft);
    }

    @SuppressWarnings("unchecked")
    default <R> Exceptional<R> invoke(InvocationContext invocationContext, boolean canHaveContextLeft) {
        final ExecutableElementContext<?> executable = this.executable();
        final Exceptional<Object[]> parameters = this.parseParameters(invocationContext, canHaveContextLeft);

        if (parameters.caught())
            return Exceptional.of(parameters.error());

        if (executable instanceof MethodContext methodContext) {
            return methodContext.invoke(this.instance(), parameters.or(new Object[0]));
        }
        ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(parameters.or(new Object[0]));

    }
}
