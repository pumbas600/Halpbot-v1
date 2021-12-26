package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActionInvokableDecorator<C extends InvocationContext> implements ActionInvokable<C>
{
    private final ActionInvokable<C> actionInvokable;

    @Override
    public @Nullable Object instance() {
        return this.actionInvokable.instance();
    }

    @Override
    public ExecutableElementContext<?> executable() {
        return this.actionInvokable.executable();
    }

    @Override
    public Exceptional<Object[]> parameters(C invocationContext) {
        return this.actionInvokable.parameters(invocationContext);
    }
}
