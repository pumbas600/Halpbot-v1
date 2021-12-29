package nz.pumbas.halpbot.commands.actioninvokable;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.jetbrains.annotations.Nullable;

@Binds(CommandInvokable.class)
public record HalpbotCommandInvokable(@Nullable Object instance,
                                      ExecutableElementContext<?, ?> executable)
        implements CommandInvokable
{
    @Bound
    public HalpbotCommandInvokable {}
}
