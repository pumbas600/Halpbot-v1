package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.Permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import nz.pumbas.halpbot.commands.exceptions.OutputException;
import nz.pumbas.halpbot.objects.Exceptional;

public interface CommandMethod
{

    /**
     * @return The {@link String description} if present, otherwise null
     */
    @NotNull
    String getDescription();

    /**
     * @return The {@link String} representation of the command
     */
    @NotNull
    String getDisplayCommand();

    /**
     * @return The {@link Object} instance for this {@link CommandMethod}. Note for static methods this will be null
     */
    @Nullable
    Object getInstance();

    /**
     * @return The {@link String permission} for this command. If there is no permission, this will an empty string
     */
    @NotNull
    Permission[] getPermissions();

    /**
     * @return The {@link Set<Long> ids} of who this command is restricted to
     */
    @NotNull
    Set<Long> getRestrictedTo();

    /**
     * @return The {@link Class classes} that can have static methods invoked from
     */
    @NotNull
    Set<Class<?>> getReflections();

    /**
     * @return The {@link String usage} for this command
     */
    @NotNull
    String getUsage();

    /**
     * Invokes the {@link Method} for this {@link CommandMethod}.
     *
     * @param args
     *     The {@link Object} arguments to be used when invoking this command
     *
     * @return An {@link Optional} containing the output of the {@link Method}
     * @throws OutputException
     *     Any {@link OutputException} thrown during the invocation of the method
     */
    Exceptional<Object> invoke(Object... args) throws OutputException;
}
