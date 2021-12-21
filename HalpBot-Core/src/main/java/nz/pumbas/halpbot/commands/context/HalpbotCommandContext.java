package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nz.pumbas.halpbot.actions.cooldowns.CooldownTimer;
import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@Getter
@Binds(CommandContext.class)
@RequiredArgsConstructor(onConstructor_ = @Bound)
public record HalpbotCommandContext(List<String> aliases,
                                    String description,
                                    String usage,
                                    @Nullable Object instance,
                                    ExecutableElementContext<?> executable,
                                    ParsingContext parsingContext,
                                    List<Token> tokens,
                                    List<String> permissions,
                                    Set<TypeContext<?>> reflections)
        implements CommandContext
{

    @Override
    public String toString() {
        return "%s %s".formatted(this.aliasesString(), this.usage).trim();
    }
}
