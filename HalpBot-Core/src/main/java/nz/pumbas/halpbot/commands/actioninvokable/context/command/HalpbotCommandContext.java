package nz.pumbas.halpbot.commands.actioninvokable.context.command;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.Content;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@ComponentBinding(CommandContext.class)
public record HalpbotCommandContext(List<String> aliases,
                                    String description,
                                    String usage,
                                    ActionInvokable<CommandInvocationContext> actionInvokable,
                                    List<Token> tokens,
                                    Set<TypeContext<?>> reflections,
                                    Duration displayDuration,
                                    boolean isEphemeral,
                                    boolean preserveWhitespace,
                                    Content content)
        implements CommandContext
{

    @Bound
    public HalpbotCommandContext { }

    @Override
    public String toString() {
        return "%s %s".formatted(this.aliasesString(), this.usage).trim();
    }
}
