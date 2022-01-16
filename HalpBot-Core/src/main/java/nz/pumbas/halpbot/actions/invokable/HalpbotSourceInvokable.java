package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.jetbrains.annotations.Nullable;

@ComponentBinding(SourceInvokable.class)
public record HalpbotSourceInvokable(@Nullable Object instance,
                                     ExecutableElementContext<?, ?> executable)
    implements SourceInvokable<SourceInvocationContext>
{
    @Bound
    public HalpbotSourceInvokable { }
}
