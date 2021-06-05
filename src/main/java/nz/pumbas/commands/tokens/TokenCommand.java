package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.exceptions.OutputException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * A container for the data of an {@link CommandToken} command.
 */
public class TokenCommand implements CommandMethod
{

    private final @Nullable Object instance;
    private final @NotNull Executable executable;
    private final @NotNull List<CommandToken> commandTokens;
    private @Nullable String description;

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable, @NotNull List<CommandToken> commandTokens) {
        this.instance = instance;
        this.executable = executable;
        this.commandTokens = commandTokens;
    }

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable,
                        @NotNull List<CommandToken> commandTokens, String description) {
        this.instance = instance;
        this.executable = executable;
        this.commandTokens = commandTokens;
        this.description = description;
    }

    /**
     * @return The {@link Executable} for this {@link nz.pumbas.commands.annotations.Command}
     */
    @NotNull
    public Executable getExecutable() {
        return this.executable;
    }

    /**
     * @return An {@link List} of {@link CommandToken command tokens} representing this {@link nz.pumbas.commands.annotations.Command}
     */
    @NotNull
    public List<CommandToken> getCommandTokens() {
        return this.commandTokens;
    }

    /**
     * @return The an array of the {@link Class parameter types} of the {@link Executable} for this {@link nz.pumbas.commands.annotations.Command}
     */
    @NotNull
    public Class<?>[] getParameterTypes() {
        return this.executable.getParameterTypes();
    }

    /**
     * Invokes the {@link Executable} for this {@link nz.pumbas.commands.annotations.Command} with the specified arguments.
     *
     * @param invocationTokens
     *      The {@link List} of {@link String invocation tokens} which will be used to invoke the {@link Executable} with
     * @param event
     *      An optional {@link MessageReceivedEvent} if that is to be passed to the method
     *
     * @return An {@link Optional} containing the result of the {@link Executable} if there is one
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    public Optional<Object> invoke(List<String> invocationTokens, @Nullable MessageReceivedEvent event)
        throws OutputException
    {
        Object[] parameters = this.parseInvocationTokens(invocationTokens, event);
        return this.invoke(parameters);
    }

    /**
     * @return If the {@link CommandMethod} has a description
     */
    @Override
    public boolean hasDescription()
    {
        return null != this.description && !this.description.isEmpty();
    }

    /**
     * @return The {@link String description} if present, otherwise null
     */
    @Override
    @Nullable
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Invokes the {@link Executable} for this {@link nz.pumbas.commands.annotations.Command} with the specified arguments.
     *
     * @param parameters
     *      The {@link Object parameters} which will be used to invoke the {@link Executable} with
     *
     * @return An {@link Optional} containing the result of the {@link Executable} if there is one
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    @SuppressWarnings("ThrowInsideCatchBlockWhichIgnoresCaughtException")
    public Optional<Object> invoke(Object... parameters) throws OutputException
    {
        try {
            if (this.executable instanceof Method)
                return Optional.ofNullable(((Method) this.executable).invoke(this.instance, parameters));
            else if (this.executable instanceof Constructor<?>)
                return Optional.of(((Constructor<?>) this.executable).newInstance(parameters));
        }
        catch (java.lang.IllegalAccessException | InstantiationException e) {
            ErrorManager.handle(e, String.format("There was an error invoking the command method, %s",
                this.executable.getName()));
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof OutputException)
                throw (OutputException) e.getTargetException();
            else ErrorManager.handle(e, String.format("There was an error thrown within the command method, %s",
                this.executable.getName()));
        }

        return Optional.empty();
    }

    /**
     * Parses the {@link List<String> invocation tokens} and the {@link MessageReceivedEvent} event into an array
     * that can can be passed to an {@link Method} when being invoked.
     *
     * @param invocationTokens
     *      The {@link List<String> invocation tokens} that are to be parsed into objects
     * @param event
     *      An optional {@link MessageReceivedEvent}. If present, it will be inserted as the first parameter in the
     *      array
     *
     * @return A {@link Object array} containing the parsed {@link List<String> invocation tokens}
     */
    public Object[] parseInvocationTokens(List<String> invocationTokens, @Nullable MessageReceivedEvent event) {
        Object[] parsedTokens = new Object[this.executable.getParameterCount()];

        int parameterIndex = 0;
        int invocationTokenIndex = 0;

        Class<?>[] parameterTypes = this.executable.getParameterTypes();
        if (null != event && 0 < parameterTypes.length && parameterTypes[0].isAssignableFrom(MessageReceivedEvent.class)) {
            parsedTokens[0] = event;
            parameterIndex = 1;
        }

        for (CommandToken currentCommandToken : this.commandTokens) {
            if (invocationTokenIndex >= invocationTokens.size()) {
                if (!currentCommandToken.isOptional())
                    throw new IllegalArgumentException(
                            String.format("There was an error parsing the invocation tokens %s, as they don't match " +
                                "this command. Make sure to check the invocation tokens match by first calling the " +
                                "matches method.", invocationTokens));
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
                        String.format("There was an error parsing the invocation tokens %s, as they don't match this " +
                            "command. Make sure to check the invocation tokens match by first calling the matches " +
                            "method.", invocationTokens));
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

        //Return true IF there is no other invocation tokens left to be checked
        return invocationTokenIndex >= invocationTokens.size();
    }
}
