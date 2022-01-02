package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.jetbrains.annotations.Nullable;

@Service
public interface InvokableFactory
{
    @Factory
    Invokable create(@Nullable Object instance, ExecutableElementContext<?, ?> executable);
}
