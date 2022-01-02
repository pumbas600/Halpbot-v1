package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.function.CheckedFunction;

import nz.pumbas.halpbot.actions.methods.Invokable;

public interface ActionInvokable<C extends InvocationContext> extends Invokable
{
    Exceptional<Object[]> parameters(C invocationContext);

    default <R> Exceptional<R> invoke(C invocationContext) {
        return this.parameters(invocationContext)
                .flatMap((CheckedFunction<Object[], Exceptional<R>>) this::invoke);
    }
}
