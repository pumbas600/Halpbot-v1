package nz.pumbas.commands.commandadapters;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.exceptions.OutputException;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationContext;

public class TokenCommandAdapter extends AbstractCommandAdapter
{
    public TokenCommandAdapter(@Nullable JDABuilder builder, String commandPrefix)
    {
        super(builder, commandPrefix);
    }

    /**
     * Creates a {@link CommandMethod} from the specified information.
     *
     * @param instance
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} for this {@link CommandMethod}
     * @param command
     *     The {@link Command} which annotates the {@link Method}
     *
     * @return The created {@link CommandMethod}
     */
    @Override
    protected CommandMethod createCommandMethod(@NotNull Object instance,
                                                @NotNull Method method,
                                                @NotNull Command command)
    {
        return TokenManager.generateTokenCommand(instance, method, command);
    }

    /**
     * Handles the invocation, matching and parsing of a {@link CommandMethod} with the given {@link String content}.
     *
     * @param event
     *     The {@link MessageReceivedEvent} which invoked the {@link CommandMethod}
     * @param commandMethod
     *     The {@link CommandMethod} which matches to the command alias that was invoked
     * @param content
     *     The rest of the {@link String} after the {@link String command alias} or null if there was nothing else
     *
     * @return If the {@link String content} matched this {@link CommandMethod} and it was invoked
     * @throws OutputException
     *     Any {@link OutputException} thrown by the {@link CommandMethod} when it was invoked
     */
    @Override
    protected boolean handleCommandMethodCall(@NotNull MessageReceivedEvent event,
                                              @NotNull CommandMethod commandMethod,
                                              @NotNull String content) throws OutputException
    {
        if (!(commandMethod instanceof TokenCommand))
            return false;

        TokenCommand tokenCommand = (TokenCommand) commandMethod;
        //If the command has been restricted and you're not whitelisted
        if (!tokenCommand.getRestrictedTo().isEmpty() && !tokenCommand.getRestrictedTo().contains(event.getAuthor().getIdLong()))
            return false;

        InvocationContext invocationToken = InvocationContext.of(content).saveState(this);
        if (tokenCommand.matches(invocationToken)) {
            Optional<Object> oResult = tokenCommand.invoke(invocationToken.restoreState(this), event, this);
            super.displayCommandMethodResult(event, oResult);

            return true;
        }

        return false;
    }
}
