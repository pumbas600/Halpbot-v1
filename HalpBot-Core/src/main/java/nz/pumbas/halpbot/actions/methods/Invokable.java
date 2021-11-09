package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.ArrayListMultiMap;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.MultiMap;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.tokens.ParsingToken;
import nz.pumbas.halpbot.commands.tokens.Token;

@SuppressWarnings("ConstantDeclaredInInterface")
public interface Invokable
{
    MultiMap<ExecutableElementContext<?>, Token> CACHE = new ArrayListMultiMap<>();

    static Collection<Token> tokens(ApplicationContext applicationContext, ExecutableElementContext<?> executable) {
        if (CACHE.containsKey(executable))
            return CACHE.get(executable);

        Collection<Token> tokens = HartshornUtils.emptyList();
        for (ParameterContext<?> parameterContext : executable.parameters()) {
            tokens.add(
                ParsingToken.of(applicationContext,
                    parameterContext.type(),
                    parameterContext.annotations()));
        }
        CACHE.putAll(executable, tokens);
        return tokens;
    }

    @NotNull
    ApplicationContext applicationContext();

    @NotNull
    ExecutableElementContext<?> executable();

    @Nullable
    Object instance();

    default Object[] parseParameters(InvocationContext invocationContext) {
        final Collection<Token> tokens = tokens(this.applicationContext(), this.executable());
        return new Object[] { };
        //TODO: parse parameters
    }

    @SuppressWarnings("unchecked")
    default <R, P> Exceptional<R> invoke(InvocationContext invocationContext) {
        final ExecutableElementContext<?> executable = this.executable();
        final Object[] parameters = this.parseParameters(invocationContext);

        if (executable instanceof MethodContext) {
            return ((MethodContext<R, P>) executable).invoke((P) this.instance(), parameters);
        }
        ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(parameters);

    }
}
