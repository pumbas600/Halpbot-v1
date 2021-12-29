package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.events.HalpbotEvent;

public interface InvocationContext extends ContextCarrier
{
    @Nullable
    HalpbotEvent halpbotEvent();

    TypeContext<?> currentType();

    void currentType(TypeContext<?> typeContext);

    String contextString();
}
