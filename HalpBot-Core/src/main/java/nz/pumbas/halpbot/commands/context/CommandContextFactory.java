package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.converters.tokens.Token;

@Service
public interface CommandContextFactory
{
    @Factory
    CommandContext create(List<String> aliases,
                          String description,
                          String usage,
                          ActionInvokable<CommandInvocationContext> actionInvokable,
                          List<Token> tokens,
                          List<String> permissions,
                          Set<TypeContext<?>> reflections);
}
