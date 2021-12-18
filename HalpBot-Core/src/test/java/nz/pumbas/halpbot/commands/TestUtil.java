package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

public class TestUtil
{
    public static MethodContext<?, ?> method(Class<?> type, String name) {
        return TypeContext.of(type).methods()
                .stream()
                .filter(method -> method.name().equals(name))
                .findFirst()
                .get();
    }
}
