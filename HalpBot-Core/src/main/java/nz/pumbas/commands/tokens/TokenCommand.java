package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.exceptions.OutputException;
import nz.pumbas.commands.tokens.context.InvocationContext;
import nz.pumbas.commands.tokens.tokentypes.CommandToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.commands.tokens.tokentypes.PlaceholderToken;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;
import nz.pumbas.utilities.Reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private final @NotNull String permission;
    private final @NotNull Set<Long> restrictedTo;
    private final @NotNull Set<Class<?>> reflections;

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable, @NotNull List<CommandToken> commandTokens) {
        this(instance, executable, commandTokens, Collections.emptySet());
    }

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable,
                        @NotNull List<CommandToken> commandTokens,
                        @NotNull Set<Class<?>> reflections) {
        this(instance, executable, commandTokens, "", "", "",
            Collections.emptySet(), reflections);
    }

    public TokenCommand(@Nullable Object instance, @NotNull Executable executable,
                        @NotNull List<CommandToken> commandTokens, @NotNull String displayCommand,
                        @NotNull String description, @NotNull String permission, @NotNull Set<Long> restrictedTo,
                        @NotNull Set<Class<?>> reflections)
    {
        this.instance = instance;
        this.executable = executable;
        this.commandTokens = commandTokens;
        this.displayCommand = displayCommand;
        this.description = description;
        this.permission = permission;
        this.restrictedTo = restrictedTo;
        this.reflections = reflections;
    }

    /**
     * @return The {@link Executable} for this {@link nz.pumbas.commands.annotations.Command}
     */
    public @NotNull Executable getExecutable() {
        return this.executable;
    }

    /**
     * @return An {@link List} of {@link CommandToken command tokens} representing this {@link nz.pumbas.commands.annotations.Command}
     */
    public @NotNull List<CommandToken> getCommandTokens() {
        return this.commandTokens;
    }

    /**
     * @return The an array of the {@link Class parameter types} of the {@link Executable} for this {@link nz.pumbas.commands.annotations.Command}
     */
    public @NotNull Class<?>[] getParameterTypes() {
        return this.executable.getParameterTypes();
    }

    /**
     * @return The {@link String description}. Note if there is no description this will be empty
     */
    @Override
    public @NotNull String getDescription()
    {
        return this.description;
    }

    /**
     * @return The {@link String} representation of the command
     */
    @Override
    public @NotNull String getDisplayCommand()
    {
        return this.displayCommand;
    }

    /**
     * @return The {@link String permission} for this command. If there is no permission, this will an empty string
     */
    @Override
    public @NotNull String getPermission()
    {
        return this.permission;
    }

    /**
     * @return The {@link List<Long>} of user ids for who this command is restricted to
     */
    @Override
    public @NotNull Set<Long> getRestrictedTo()
    {
        return this.restrictedTo;
    }

    /**
     * @return The {@link Class classes} that can have static methods invoked from
     */
    @Override
    public @NotNull Set<Class<?>> getReflections()
    {
        return this.reflections;
    }

    /**
     * @return The {@link Object} instance for this {@link CommandMethod}
     */
    @Override
    public @Nullable Object getInstance()
    {
        return this.instance;
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
     * parses the {@link InvocationContext} and invokes the {@link Executable} for this
     * {@link nz.pumbas.commands.annotations.Command}.
     *
     * @param context
     *      The {@link InvocationContext}
     *
     * @return A {@link Result} containing the returned value of the {@link Executable}
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    public Result<Object> parse(@NotNull InvocationContext context)
    {
        return this.parse(context, null, null);
    }

    /**
     * parses the {@link InvocationContext} and invokes the {@link Executable} for this
     * {@link nz.pumbas.commands.annotations.Command}.
     *
     * @param context
     *      The {@link InvocationContext}
     * @param event
     *      The {@link MessageReceivedEvent}
     * @param commandAdapter
     *      The {@link AbstractCommandAdapter}
     *
     * @return A {@link Result} containing the returned value of the {@link Executable}
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    public Result<Object> parse(@NotNull InvocationContext context,
                                @Nullable MessageReceivedEvent event,
                                @Nullable AbstractCommandAdapter commandAdapter)
    {
        Result<Object[]> parsedParameters = this.parseParameters(context, event, commandAdapter);
        return parsedParameters.hasValue()
            ? Result.of(this.invoke(parsedParameters.getValue()))
            : parsedParameters.cast();
    }

    /**
     * Parses the {@link InvocationContext} into an array of {@link Object objects} which can be used to invoke this
     * {@link Executable}.
     *
     * @param context
     *      The {@link InvocationContext}
     * @param event
     *      The {@link MessageReceivedEvent}
     * @param commandAdapter
     *      The {@link AbstractCommandAdapter}
     *
     * @return A {@link Result} containing the parsed parameters
     */
    public Result<Object[]> parseParameters(@NotNull InvocationContext context,
                                            @Nullable MessageReceivedEvent event,
                                            @Nullable AbstractCommandAdapter commandAdapter)
    {
        Object[] parsedTokens = new Object[this.executable.getParameterCount()];
        Class<?>[] parameterTypes = this.executable.getParameterTypes();

        int tokenIndex = 0;
        int parameterIndex = 0;
        Result<Object[]> firstMismatch = Result.empty();

        while (parameterIndex < parsedTokens.length) {
            Optional<Class<?>> assignableTo = Reflect.getAssignableTo(parameterTypes[parameterIndex],
                TokenManager.getCustomParameterTypes());

            if (assignableTo.isPresent()) {
                parsedTokens[parameterIndex++] = TokenManager.getCustomParameterMapper(assignableTo.get())
                        .apply(event, commandAdapter);
                continue;
            }

            if (tokenIndex >= this.commandTokens.size())
                return firstMismatch.orIfEmpty(Result.of(Resource.get("halpbot.commands.match.tokenexcess")));

            CommandToken currentCommandToken = this.commandTokens.get(tokenIndex++);
            Result<Object[]> mismatchResult = Result.empty();
            context.saveState(this);

            if (!context.hasNext()) {
                if (currentCommandToken.isOptional()) {
                    if (currentCommandToken instanceof ParsingToken)
                        parsedTokens[parameterIndex++] = ((ParsingToken) currentCommandToken).getDefaultValue();
                    continue;
                }

                if (firstMismatch.isEmpty())
                    firstMismatch = Result.of(Resource.get("halpbot.commands.match.tokendeficit"));

                return firstMismatch;
            }
            else if (currentCommandToken instanceof ParsingToken) {
                Result<Object> invokedMethodResult = TokenManager.handleReflectionSyntax(context,
                    this.getReflections(), ((ParsingToken)currentCommandToken).getType());
                if (invokedMethodResult.hasValue()) {
                    parsedTokens[parameterIndex++] = invokedMethodResult.getValue();
                    continue;
                }
                else if (invokedMethodResult.hasReason())
                    return invokedMethodResult.cast();

                context.restoreState(this);

                Result<Object> result = ((ParsingToken) currentCommandToken).parse(context);
                if (result.hasValue()) {
                    if (!firstMismatch.isEmpty())
                        firstMismatch = Result.empty();

                    parsedTokens[parameterIndex++] = result.getValue();
                    continue;
                }
                mismatchResult = result.cast();
            }
            else if (currentCommandToken instanceof PlaceholderToken) {
                Result<Boolean> result = ((PlaceholderToken) currentCommandToken).matches(context);
                if (result.getValue()) {
                    if (!firstMismatch.isEmpty())
                        firstMismatch = Result.empty();
                    continue;
                }
                mismatchResult = Result.of(result.getReason());
            }


            if (firstMismatch.isEmpty())
                firstMismatch = mismatchResult;

            if (currentCommandToken.isOptional()) {
                if (currentCommandToken instanceof ParsingToken)
                    parsedTokens[parameterIndex++] = ((ParsingToken) currentCommandToken).getDefaultValue();
                context.restoreState(this);
            }
            else return firstMismatch;
        }

        if (context.hasNext())
            return firstMismatch.orIfEmpty(Result.of(Resource.get("halpbot.commands.match.tokenexcess")));
        return Result.of(parsedTokens);
    }
}
