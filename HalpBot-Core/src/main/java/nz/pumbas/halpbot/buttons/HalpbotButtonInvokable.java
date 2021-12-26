package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.jetbrains.annotations.Nullable;

@Binds(ButtonInvokable.class)
public record HalpbotButtonInvokable(@Nullable Object instance,
                                     ExecutableElementContext<?> executable)
    implements ButtonInvokable
{
    @Bound
    public HalpbotButtonInvokable { }
}
