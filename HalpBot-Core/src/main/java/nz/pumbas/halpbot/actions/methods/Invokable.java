package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

public interface Invokable
{
    @Nullable
    Object instance();

    ExecutableElementContext<?, ?> executable();

    <R> Exceptional<R> invoke(Object... parameters);
}
