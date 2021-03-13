package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

import nz.pumbas.commands.Annotations.CustomParameter;
import nz.pumbas.commands.Exceptions.IllegalCustomParameterException;
import nz.pumbas.utilities.Utilities;

public class CustomParameterType
{
    private final @NotNull String alias;
    private final @NotNull Class<?> type;
    private final @NotNull String constructor;

    public CustomParameterType(@NotNull Class<?> type, @NotNull CustomParameter customParameter)
    {
        this.alias = Utilities.isEmpty(customParameter.alias()) ? type.getSimpleName().toUpperCase() : customParameter.alias();
        this.type = type;

        if (0 == this.type.getDeclaredConstructors().length)
            throw new IllegalCustomParameterException(
                String.format("The custom parameter %s (%s), must define a constructor",
                    this.alias, this.type.getSimpleName()));

        Constructor<?> constructor = this.type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        if (Utilities.isEmpty(customParameter.constructor())) {
            this.constructor = CommandManager.automaticallyGenerateCommand(constructor.getParameterTypes());
        } else {
            this.constructor = customParameter.constructor();
        }
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
