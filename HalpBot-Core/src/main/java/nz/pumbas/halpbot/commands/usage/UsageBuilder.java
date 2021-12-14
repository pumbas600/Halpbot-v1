package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

@FunctionalInterface
public interface UsageBuilder
{
    default boolean isValid(ApplicationContext applicationContext) {
        return true;
    }

    String buildUsage(ApplicationContext applicationContext, ExecutableElementContext<?> executableContext);
}
