package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.function.CheckedFunction;

import nz.pumbas.halpbot.actions.methods.Invokable;

public interface ActionInvokable<C extends InvocationContext> extends Invokable
{
    Exceptional<Object[]> parameters(C invocationContext);

    @Override
    @SuppressWarnings("unchecked")
    default <R> Exceptional<R> invoke(Object... parameters) {
        final ExecutableElementContext<?> executable = this.executable();
        if (executable instanceof MethodContext methodContext) {
            return methodContext.invoke(this.instance(), parameters);
        }
        ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(parameters);
    }

    default <R> Exceptional<R> invoke(C invocationContext) {
        return this.parameters(invocationContext)
                .flatMap((CheckedFunction<Object[], Exceptional<R>>) this::invoke);
    }
}
