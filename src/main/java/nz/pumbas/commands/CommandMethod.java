package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class CommandMethod
{

    private final @NotNull Method method;
    private final @NotNull Object object;
    private final boolean hasParamaters;

    public CommandMethod(@NotNull Method method, @NotNull Object object)
    {
        this.method = method;
        this.object = object;

        this.hasParamaters = 0 != method.getParameterCount();
    }

    public void InvokeMethod(Object... args)
    {
        Object[] methodArgs = this.hasParamaters ? args : new Object[0];

        try {
            this.method.invoke(this.object, methodArgs);
        } catch (java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException ignored) {
        }
    }
}
