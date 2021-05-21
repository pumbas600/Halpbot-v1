package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import nz.pumbas.commands.ErrorManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A container for the data of an {@link CommandToken} command.
 */
public class TokenCommand {

    private final @Nullable Object instance;
    private final @NotNull Executable executable;
    private final @NotNull List<CommandToken> commandTokens;

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable, @NotNull List<CommandToken> commandTokens) {
        this.instance = instance;
        this.executable = executable;
        this.commandTokens = commandTokens;
    }

    /**
     * @return The {@link Executable} for this {@link nz.pumbas.commands.Annotations.Command}
     */
    @NotNull
    public Executable getExecutable() {
        return this.executable;
    }

    /**
     * @return An {@link List} of {@link CommandToken command tokens} representing this {@link nz.pumbas.commands.Annotations.Command}
     */
    @NotNull
    public List<CommandToken> getCommandTokens() {
        return this.commandTokens;
    }

    /**
     * @return The an array of the {@link Class parameter types} of the {@link Executable} for this {@link nz.pumbas.commands.Annotations.Command}
     */
    @NotNull
    public Class<?>[] getParameterTypes() {
        return this.executable.getParameterTypes();
    }

    /**
     * Invokes the {@link Executable} for this {@link nz.pumbas.commands.Annotations.Command} with the specified arguments.
     *
     * @param invocationTokens
     *      The {@link List} of {@link String invocation tokens} which will be used to invoke the {@link Executable} with
     *
     * @return An {@link Optional} containing the result of the {@link Executable} if there is one
     * @throws InvocationTargetException Any exception thrown within the {@link Executable}
     */
    public Optional<Object> invoke(List<String> invocationTokens, @Nullable MessageReceivedEvent event) throws InvocationTargetException
    {
        Object[] parameters = parseInvocationTokens(invocationTokens, event);

        try {
            if (executable instanceof Method)
                return Optional.ofNullable(((Method) this.executable).invoke(this.instance, parameters));
            else if (executable instanceof Constructor<?>)
                return Optional.of(((Constructor<?>) this.executable).newInstance(parameters));
        } catch (java.lang.IllegalAccessException | InstantiationException e) {
            ErrorManager.handle(e, String.format("There was an error invoking the command method, %s",
                    this.executable.getName()));
        }

        return Optional.empty();
    }

    public Object[] parseInvocationTokens(List<String> invocationTokens, @Nullable MessageReceivedEvent event) {
        Object[] parsedTokens = new Object[this.executable.getParameterCount()];

        int parameterIndex = 0;
        int invocationTokenIndex = 0;

        if (event != null) {
            parsedTokens[0] = event;
            parameterIndex = 1;
        }

        for (CommandToken currentCommandToken : this.commandTokens) {
            if (invocationTokenIndex >= invocationTokens.size()) {
                if (!currentCommandToken.isOptional())
                    throw new IllegalArgumentException(
                            String.format("There was an error parsing the invocation tokens %s, as they don't match this command. " +
                                    "Make sure to check the invocation tokens match by first calling the matches method.", invocationTokens.toString()));
                continue;
            }

            String currentInvocationToken = invocationTokens.get(invocationTokenIndex);

            if (currentCommandToken.matches(currentInvocationToken)) {
                if (currentCommandToken instanceof ParsingToken) {
                    parsedTokens[parameterIndex] = ((ParsingToken) currentCommandToken).parse(currentInvocationToken);
                    parameterIndex++;
                }
                invocationTokenIndex++;
            }
            else if (currentCommandToken.isOptional()) {
                if (currentCommandToken instanceof ParsingToken) {
                    parsedTokens[parameterIndex] = ((ParsingToken) currentCommandToken).getDefaultValue();
                    parameterIndex++;
                }
            }
            else {
                throw new IllegalArgumentException(
                        String.format("There was an error parsing the invocation tokens %s, as they don't match this command. " +
                                "Make sure to check the invocation tokens match by first calling the matches method.", invocationTokens.toString()));
            }
        }

        return parsedTokens;
    }

    /**
     * Determines if the {@link List} of {@link String invocation tokens} match with this {@link TokenCommand}.
     *
     * @param invocationTokens
     *      The {@link List} of {@link String invocation tokens} to check that matches with this {@link TokenCommand}
     *
     * @return If the {@link List} of {@link String invocation tokens} match this {@link TokenCommand}
     */
    public boolean matches(List<String> invocationTokens)
    {
        int invocationTokenIndex = 0;
        for (CommandToken currentCommandToken : this.commandTokens) {
            if (invocationTokenIndex >= invocationTokens.size()) {
                if (!currentCommandToken.isOptional())
                    return false;
                continue;
            }
            String currentInvocationToken = invocationTokens.get(invocationTokenIndex);

            // If they match, move to the next token
            if (currentCommandToken.matches(currentInvocationToken))
                invocationTokenIndex++;
            // If it doesn't match but the current command token is optional, it checks if the next invocation token matches
            // Otherwise, if it doesn't match and its not optional, then these invocations don't match this command
            else if (!currentCommandToken.isOptional() || invocationTokenIndex == invocationTokens.size() -1) {
                return false;
            }
        }
        return true;
    }
}
