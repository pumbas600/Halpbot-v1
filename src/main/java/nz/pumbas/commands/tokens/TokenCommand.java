package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.exceptions.OutputException;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.commands.tokens.tokentypes.CommandToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.Reflect;

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
    private final @NotNull String displayCommand;
    private final @NotNull String description;

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable, @NotNull List<CommandToken> commandTokens) {
        this.instance = instance;
        this.executable = executable;
        this.commandTokens = commandTokens;
        this.description = "";
        this.displayCommand = "";
    }

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable,
                        @NotNull List<CommandToken> commandTokens, @NotNull String displayCommand,
                        @NotNull String description) {
        this.instance = instance;
        this.executable = executable;
        this.commandTokens = commandTokens;
        this.displayCommand = displayCommand;
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
     * @return If the {@link CommandMethod} has a description
     */
    @Override
    public boolean hasDescription()
    {
        return !this.description.isEmpty();
    }

    /**
     * @return The {@link String description} if present, otherwise null
     */
    @Override
    @NotNull
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return The {@link String} representation of the command
     */
    @Override
    @NotNull
    public String getDisplayCommand()
    {
        return this.displayCommand;
    }

    /**
     * Invokes the {@link Executable} for this {@link nz.pumbas.commands.annotations.Command} with the specified arguments.
     *
     * @param invocationToken
     *      The {@link List} of {@link String invocation tokens} which will be used to invoke the {@link Executable} with
     * @param event
     *      The {@link MessageReceivedEvent} that invoked this command
     * @param commandAdapter
     *      The {@link AbstractCommandAdapter} which manages this command
     *
     * @return An {@link Optional} containing the result of the {@link Executable} if there is one
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    public Optional<Object> invoke(@NotNull InvocationTokenInfo invocationToken,
                                   @Nullable MessageReceivedEvent event,
                                   @Nullable AbstractCommandAdapter commandAdapter) throws OutputException
    {
        Object[] parameters = this.parseInvocationTokens(invocationToken, event, commandAdapter);
        return this.invoke(parameters);
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
    public Optional<Object> invoke(@NotNull Object... parameters) throws OutputException
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
     * Parses the {@link InvocationTokenInfo invocation tokens} into an array
     * that can can be passed to an {@link Method} when being invoked.
     *
     * @param invocationToken
     *      The {@link InvocationTokenInfo invocation tokens} that are to be parsed into objects
     * @param event
     *      The {@link MessageReceivedEvent} that invoked this command
     * @param commandAdapter
     *      The {@link AbstractCommandAdapter} which manages this command
     *
     * @return A {@link Object array} containing the parsed {@link List<String> invocation tokens}
     */
    public Object[] parseInvocationTokens(@NotNull InvocationTokenInfo invocationToken,
                                          @Nullable MessageReceivedEvent event,
                                          @Nullable AbstractCommandAdapter commandAdapter)
    {
        Object[] parsedTokens = new Object[this.executable.getParameterCount()];
        Class<?>[] parameterTypes = this.executable.getParameterTypes();

        int tokenIndex = 0;
        for (int parameterIndex = 0; parameterIndex < parsedTokens.length; parameterIndex++) {
            Optional<Class<?>> assignableFromClass = Reflect.getAssignableFrom(
                parameterTypes[parameterIndex], TokenManager.getCustomParameterTypes());

            if (assignableFromClass.isPresent()) {
                parsedTokens[parameterIndex] =
                    TokenManager.getCustomParameterMapper(assignableFromClass.get()).apply(event, commandAdapter);
                continue;
            }

            CommandToken currentCommandToken = this.commandTokens.get(tokenIndex);
            invocationToken.saveState(this);
            if (invocationToken.hasNext() && currentCommandToken.matches(invocationToken)) {
                invocationToken.restoreState(this);
                if (currentCommandToken instanceof ParsingToken) {
                    parsedTokens[parameterIndex] = ((ParsingToken) currentCommandToken).parse(invocationToken);
                }
                tokenIndex++;
            }
            else if (currentCommandToken.isOptional()) {
                invocationToken.restoreState(this);
                if (currentCommandToken instanceof ParsingToken) {
                    parsedTokens[parameterIndex] = ((ParsingToken) currentCommandToken).getDefaultValue();
                }
                tokenIndex++;
            }
            else {
                throw new IllegalArgumentException(
                    String.format("There was an error parsing the invocation tokens %s, as they don't match this " +
                        "command. Make sure to check the invocation tokens match by first calling the matches " +
                        "method.", invocationToken.getOriginal()));
            }
        }

        return parsedTokens;
    }

    /**
     * Determines if the {@link InvocationTokenInfo invocation token} matches with this {@link TokenCommand}.
     *
     * @param invocationToken
     *      The {@link InvocationTokenInfo invocation token} to check
     *
     * @return If the {@link InvocationTokenInfo invocation tokens} match this {@link TokenCommand}
     */
    public boolean matches(InvocationTokenInfo invocationToken)
    {
        for (CommandToken currentCommandToken : this.commandTokens) {
            if (!invocationToken.hasNext()) {
                if (!currentCommandToken.isOptional())
                    return false;
                continue;
            }
            invocationToken.saveState(this);

            if (!currentCommandToken.matches(invocationToken)) {
                // If it doesn't match but the current command token is optional, it checks if the next invocation token matches
                // Otherwise, if it doesn't match and its not optional, then these invocations don't match this command
                if (currentCommandToken.isOptional() && invocationToken.hasNext())
                    invocationToken.restoreState(this);
                else
                    return false;
            }
        }

        //Return true IF there are no other invocation tokens left to be checked
        return !invocationToken.hasNext();
    }
}
