package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.tokens.HalpbotParsingToken;
import nz.pumbas.halpbot.commands.tokens.Token;

@SuppressWarnings("ConstantDeclaredInInterface")
public interface Invokable
{
    Map<ExecutableElementContext<?>, List<Token>> CACHE = HartshornUtils.emptyMap();

    static List<Token> tokens(@NotNull ApplicationContext applicationContext,
                              @NotNull ExecutableElementContext<?> executable) {
        if (CACHE.containsKey(executable))
            return CACHE.get(executable);

        List<Token> tokens = executable.parameters()
            .stream()
            .map(parameterContext -> HalpbotParsingToken.of(applicationContext, parameterContext))
            .collect(Collectors.toList());

        CACHE.put(executable, tokens);
        return tokens;
    }

    @NotNull
    ExecutableElementContext<?> executable();

    @Nullable
    Object instance();

    @NotNull
    default List<Token> tokens(@NotNull ApplicationContext applicationContext) {
        return tokens(applicationContext, this.executable());
    }

    @NotNull
    default Object[] parseParameters(@NotNull InvocationContext invocationContext) {
        final List<Token> tokens = tokens(invocationContext.applicationContext(), this.executable());
        return new Object[] { };
        //TODO: parse parameters
    }

    @SuppressWarnings("unchecked")
    default <R, P> Exceptional<R> invoke(@NotNull InvocationContext invocationContext) {
        final ExecutableElementContext<?> executable = this.executable();
        final Object[] parameters = this.parseParameters(invocationContext);

        if (executable instanceof MethodContext) {
            return ((MethodContext<R, P>) executable).invoke((P) this.instance(), parameters);
        }
        ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(parameters);

    }
}
