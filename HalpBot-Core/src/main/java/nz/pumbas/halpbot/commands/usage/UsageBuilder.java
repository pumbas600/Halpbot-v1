package nz.pumbas.halpbot.commands.usage;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

@FunctionalInterface
public interface UsageBuilder
{
    String buildUsage(ApplicationContext applicationContext, ExecutableElementContext<?> executableContext);
}
