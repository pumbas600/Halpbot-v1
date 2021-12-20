package nz.pumbas.halpbot.actions.cooldowns;

import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;


@AllArgsConstructor
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class CommandContextDecorator implements CommandContext
{
    private final CommandContext commandContext;

    @Override
    public @Nullable Object instance() {
        return this.commandContext.instance();
    }

    @Override
    public ExecutableElementContext<?> executable() {
        return this.commandContext.executable();
    }

    @Override
    public ParsingContext parsingContext() {
        return this.commandContext.parsingContext();
    }

    @Override
    public List<String> aliases() {
        return this.commandContext.aliases();
    }

    @Override
    public String description() {
        return this.commandContext.description();
    }

    @Override
    public String usage() {
        return this.commandContext.usage();
    }

    @Override
    public Set<TypeContext<?>> reflections() {
        return this.commandContext.reflections();
    }

    @Override
    public List<String> permissions() {
        return this.commandContext.permissions();
    }
}
