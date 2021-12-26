package nz.pumbas.halpbot.commands;

import java.util.List;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.converters.tokens.Token;

public interface TokenInvokable extends Invokable
{
    List<Token> tokens();
}
