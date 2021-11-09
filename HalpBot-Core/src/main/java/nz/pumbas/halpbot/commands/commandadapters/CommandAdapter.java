package nz.pumbas.halpbot.commands.commandadapters;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.commands.commandmethods.CommandContext;

@Service
public interface CommandAdapter
{
    String prefix();

    <T> void registerCommands(@NotNull TypeContext<T> typeContext);

    default Exceptional<CommandContext> commandContextSafely(String alias) {
        return Exceptional.of(this.commandContext(alias));
    }

    @Nullable
    CommandContext commandContext(String alias);
}
