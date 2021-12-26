package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.commands.context.parsing.CommandParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@Binds(CommandContext.class)
public record HalpbotCommandContext(List<String> aliases,
                                    String description,
                                    String usage,
                                    @Nullable Object instance,
                                    ExecutableElementContext<?> executable,
                                    CommandParsingContext parsingContext,
                                    List<Token> tokens,
                                    List<String> permissions,
                                    Set<TypeContext<?>> reflections)
        implements CommandContext
{

    @Bound
    public HalpbotCommandContext { }

    @Override
    public String toString() {
        return "%s %s".formatted(this.aliasesString(), this.usage).trim();
    }
}
