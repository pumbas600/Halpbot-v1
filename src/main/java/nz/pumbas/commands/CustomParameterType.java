package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

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
            List<String> constructorString = new ArrayList<>();
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                if (CommandManager.CommandTypes.containsKey(parameterType)) {
                    constructorString.add(CommandManager.CommandTypes.get(parameterType).getAlias());
                }
                else {
                    throw new IllegalCustomParameterException(
                            String.format("The type %s is not a default type. The constructor must be explicitly defined " +
                                    "in the CustomParameter annotation", parameterType));
                }
            }

            this.constructor = String.join(" ", constructorString);
        }
        else {
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
