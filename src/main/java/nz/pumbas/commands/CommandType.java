package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class CommandType
{
    private final @NotNull Class<?> type;
    private final @NotNull String alias;
    private final @NotNull String command;
    private final @NotNull Function<String, Object> typeParser;

    public CommandType(@NotNull Class<?> type, @NotNull String alias, @NotNull String command, @NotNull Function<String, Object> typeParser)
    {
        this.type = type;
        this.alias = alias;
        this.command = command;
        this.typeParser = typeParser;
    }

    public @NotNull Class<?> getType()
    {
        return this.type;
    }

    public @NotNull String getAlias()
    {
        return this.alias;
    }

    public @NotNull String getCommand()
    {
        return this.command;
    }

    public @NotNull Function<String, Object> getTypeParser()
    {
        return this.typeParser;
    }
}

