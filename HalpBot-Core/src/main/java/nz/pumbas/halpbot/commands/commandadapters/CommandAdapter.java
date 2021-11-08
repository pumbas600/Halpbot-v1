package nz.pumbas.halpbot.commands.commandadapters;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;

import nz.pumbas.halpbot.commands.commandmethods.CommandMethod;

@Service
public interface CommandAdapter
{
    String prefix();

    <T> void registerCommands(@NotNull TypeContext<T> typeContext);

    CommandMethod commandMethod(String alias);
}
