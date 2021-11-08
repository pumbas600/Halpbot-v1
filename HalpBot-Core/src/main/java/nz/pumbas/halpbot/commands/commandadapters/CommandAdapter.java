package nz.pumbas.halpbot.commands.commandadapters;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

@Service
public interface CommandAdapter
{
    String prefix();

    <T> void registerCommands(@NotNull TypeContext<T> typeContext);
}
