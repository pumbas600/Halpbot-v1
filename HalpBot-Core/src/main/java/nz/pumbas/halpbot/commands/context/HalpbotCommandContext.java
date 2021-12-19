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

@Getter
@Binds(CommandContext.class)
@RequiredArgsConstructor(onConstructor_ = @Bound)
public class HalpbotCommandContext implements CommandContext
{

    private final List<String> aliases;
    private final String description;
    private final String usage;
    private final @Nullable Object instance;
    private final ExecutableElementContext<?> executable;
    private final  List<String> permissions;
    private final Set<TypeContext<?>> reflections;
    private final ParsingContext parsingContext;
    private final long cooldownDurationMs;

    @Setter private CooldownTimer cooldownTimer;

    @Override
    public String toString() {
        return "%s %s".formatted(this.aliasesString(), this.usage).trim();
    }
}
