package nz.pumbas.halpbot.commands.commandmethods;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.commands.exceptions.OutputException;
import nz.pumbas.halpbot.commands.tokens.Token;

public record HalpbotCommandContext(@NotNull String alias,
                                    @NotNull String description,
                                    @NotNull String usage,
                                    @Nullable Object instance,
                                    @NotNull ExecutableElementContext<?> executable,
                                    @NotNull List<String> permissions,
                                    @NotNull Set<TypeContext<?>> reflections,
                                    @NotNull List<Token> tokens)
    implements CommandContext
{
    @Override
    public Exceptional<Object> invoke(Object... args) throws OutputException {
        return null;
    }
}
