package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.Annotations.CustomParameter;
import nz.pumbas.utilities.Utilities;

public class CustomParameterType
{
    private final @NotNull String alias;
    private final @NotNull Class<?> type;
    private final @NotNull String constructor;

    public CustomParameterType(@NotNull Class<?> type, @NotNull CustomParameter customParameter, @NotNull String constructor)
    {
        this.alias = Utilities.isEmpty(customParameter.alias()) ? type.getSimpleName().toUpperCase() : customParameter.alias();
        this.type = type;
        this.constructor = constructor;
    }

    public @NotNull String getTypeAlias()
    {
        return this.alias;
    }

    public @NotNull String getTypeConstructor()
    {
        return this.constructor;
    }

    public @NotNull Class<?> getType()
    {
        return this.type;
    }
}
