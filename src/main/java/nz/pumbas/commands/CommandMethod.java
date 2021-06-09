package nz.pumbas.commands;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import nz.pumbas.commands.exceptions.OutputException;

public interface CommandMethod
{
    /**
     * @return If the {@link CommandMethod} has a description
     */
    boolean hasDescription();

    /**
     * @return The {@link String description} if present, otherwise null
     */
    @Nullable
    String getDescription();

    /**
     * @return The {@link String} representation of the command
     */
    String getDisplayCommand();

    Optional<Object> invoke(Object... args) throws OutputException;
}
