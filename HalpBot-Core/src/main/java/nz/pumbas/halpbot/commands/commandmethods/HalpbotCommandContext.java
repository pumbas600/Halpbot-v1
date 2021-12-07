package nz.pumbas.halpbot.commands.commandmethods;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.commands.commandmethods.parsing.ParsingContext;

@Binds(CommandContext.class)
public record HalpbotCommandContext(List<String> aliases,
                                    String description,
                                    String usage,
                                    @Nullable Object instance,
                                    ExecutableElementContext<?> executable,
                                    List<String> permissions,
                                    Set<TypeContext<?>> reflections,
                                    ParsingContext parsingContext)
    implements CommandContext
{

    @Bound
    public HalpbotCommandContext {}
}
