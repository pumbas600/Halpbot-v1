package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;

public interface CommandInvokable extends ActionInvokable<CommandInvocationContext>
{
    //TODO: Replace ParsingContext with this
    @Override
    default Exceptional<Object[]> parameters(CommandInvocationContext invocationContext) {
        return Exceptional.of(new Object[0]);
    }
}
