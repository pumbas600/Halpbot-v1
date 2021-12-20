package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.jetbrains.annotations.Nullable;

@Binds(DecoratorContext.class)
public record HalpbotDecoratorContext(ExecutableElementContext<?> executable,
                                      @Nullable Object instance)
        implements DecoratorContext
{
    @Bound
    public HalpbotDecoratorContext { }
}
