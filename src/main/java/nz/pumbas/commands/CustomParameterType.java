package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomParameterType
{
    private final @NotNull String alias;
    private final @NotNull Class<?> type;
    private final @NotNull List<ConstructorPair> constructors;

    public CustomParameterType(@NotNull Class<?> type, @NotNull List<ConstructorPair> constructors)
    {
        this.alias = type.getSimpleName().toUpperCase();
        this.type = type;
        this.constructors = constructors;
    }

    public @NotNull String getTypeAlias()
    {
        return this.alias;
    }

    public @NotNull List<ConstructorPair> getTypeConstructors()
    {
        return this.constructors;
    }

    public @NotNull Class<?> getType()
    {
        return this.type;
    }
}
