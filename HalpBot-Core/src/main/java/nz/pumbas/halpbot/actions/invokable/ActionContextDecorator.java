package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

public interface ActionContextDecorator<C extends InvocationContext> extends ActionInvokable<C>
{
    ActionInvokable<C> actionInvokable();

    @Override
    default Exceptional<Object[]> parameters(C invocationContext) {
        return this.actionInvokable().parameters(invocationContext);
    }

    @Override
    @Nullable
    default Object instance() {
        return this.actionInvokable().instance();
    }

    @Override
    default ExecutableElementContext<?, ?> executable() {
        return this.actionInvokable().executable();
    }

    @Override
    default <R> Exceptional<R> invoke(C invocationContext) {
        return this.actionInvokable().invoke(invocationContext);
    }

    @Override
    default <R> Exceptional<R> invoke(Object... parameters) {
        return this.actionInvokable().invoke(parameters);
    }
}
