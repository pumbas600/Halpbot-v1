package nz.pumbas.halpbot.commands.commandmethods;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public record HalpbotCommandContext(@NotNull List<String> aliases,
                                    @NotNull String description,
                                    @NotNull String usage,
                                    @Nullable Object instance,
                                    @NotNull ExecutableElementContext<?> executable,
                                    @NotNull List<String> permissions,
                                    @NotNull Set<TypeContext<?>> reflections)
    implements CommandContext
{

}
