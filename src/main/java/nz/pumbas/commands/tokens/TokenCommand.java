package nz.pumbas.commands.tokens;

import nz.pumbas.commands.ErrorManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * A container for the data of an {@link CommandToken} command.
 */
public class TokenCommand {

    private final @NotNull Object instance;
    private final @NotNull Method method;
    private final @NotNull List<CommandToken> commandTokens;

    public TokenCommand(@NotNull Object instance, @NotNull Method method, @NotNull List<CommandToken> commandTokens) {
        this.instance = instance;
        this.method = method;
        this.commandTokens = commandTokens;
    }

    /**
     * @return The {@link Method} for this {@link nz.pumbas.commands.Annotations.Command}
     */
    @NotNull
    public Method getMethod() {
        return this.method;
    }

    /**
     * @return An {@link List} of {@link CommandToken command tokens} representing this {@link nz.pumbas.commands.Annotations.Command}
     */
    @NotNull
    public List<CommandToken> getCommandTokens() {
        return this.commandTokens;
    }

    /**
     * @return The an array of the {@link Class parameter types} of the {@link Method} for this {@link nz.pumbas.commands.Annotations.Command}
     */
    @NotNull
    public Class<?>[] getParameterTypes() {
        return this.method.getParameterTypes();
    }

    /**
     * Invokes the {@link Method} for this {@link nz.pumbas.commands.Annotations.Command} with the specified arguments.
     *
     * @param args
     *      The {@link Object} arguments to invoke the {@link Method} with
     *
     * @return An {@link Optional} containing the result of the {@link Method} if there is one
     * @throws InvocationTargetException Any exception thrown within the {@link Method}
     */
    public Optional<Object> InvokeMethod(Object... args) throws InvocationTargetException
    {
        try {
            return Optional.ofNullable(this.method.invoke(this.instance, args));
        } catch (java.lang.IllegalAccessException e) {
            ErrorManager.handle(e, String.format("There was an error invoking the command method, %s",
                    this.method.getName()));
        }

        return Optional.empty();
    }

    /**
     * Determines if the {@link List} of {@link String invocation tokens} match with this {@link TokenCommand}.
     *
     * @param invocationTokens
     *      The {@link List} of {@link String invocation tokens} to check that matches with this {@link TokenCommand}
     * @return
     */
    public boolean matches(List<String> invocationTokens)
    {
        return false; //TODO: This
    }
}
