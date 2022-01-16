package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.jetbrains.annotations.Nullable;

@ComponentBinding(Invokable.class)
public record HalpbotInvokable(@Nullable Object instance,
                               ExecutableElementContext<?, ?> executable)
    implements Invokable
{
    @Bound
    public HalpbotInvokable { }
}
