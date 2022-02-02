package nz.pumbas.halpbot.commands.actioninvokable.context.command;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.Content;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
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
                          Set<TypeContext<?>> reflections,
                          Duration displayDuration,
                          boolean isEphemeral,
                          boolean preserveWhitespace,
                          Content content);
}
