package nz.pumbas.halpbot.buttons;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

@Binds(ButtonContext.class)
public record HalpbotButtonContext(String id,
                                   boolean isEphemeral,
                                   @Nullable Object instance,
                                   ExecutableElementContext<?> executable)
    implements ButtonContext
{
    @Bound
    public HalpbotButtonContext {}
}
