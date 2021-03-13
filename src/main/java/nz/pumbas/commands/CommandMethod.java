package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.utilities.Utilities;

public class CommandMethod
{

    private final @NotNull Method method;
    private final @NotNull Object object;
    private final @NotNull Command commandAnotation;

    //Save this, as it will be repeatedly checked
    private final boolean hasCommand;
    private Pattern command;

    public CommandMethod(@NotNull Method method, @NotNull Object object, @NotNull Command commandAnnotation)
    {
        this.method = method;
        this.object = object;
        this.commandAnotation = commandAnnotation;

        this.hasCommand = 1 < method.getParameterCount();

        if (this.hasCommand) {
            String command = Utilities.isEmpty(commandAnnotation.command())
                ? CommandManager.automaticallyGenerateCommand(method.getParameterTypes())
                : commandAnnotation.command();

            this.command = Pattern.compile(CommandManager.formatCommand(command));
        }
    }

    public void InvokeMethod(Object... args) throws InvocationTargetException
    {
        try {
            this.method.invoke(this.object, args);
        } catch (java.lang.IllegalAccessException e) {
            CommandManager.handle(e, String.format("There was an error invoking the command method, %s",
                this.method.getName()));
        }
    }

    public @NotNull Method getMethod()
    {
        return this.method;
    }

    public @NotNull String getDescription()
    {
        return this.commandAnotation.description();
    }

    public Pattern getCommand()
    {
        return this.command;
    }

    public boolean hasCommand()
    {
        return this.hasCommand;
    }

    public boolean hasParamaters()
    {
        return 0 != this.method.getParameterCount();
    }

    public boolean hasDescription()
    {
        return !Utilities.isEmpty(this.getDescription());
    }
}
