package nz.pumbas.halpbot.commands;

import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.parsing.CommandParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;

public interface TokenInvokable extends ActionInvokable<CommandParsingContext, InvocationContext>
{
    List<Token> tokens();
}
