package nz.pumbas.commands;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.utilities.Utilities;

public class CommandMethod
{

    private final @NotNull Method method;
    private final @NotNull Object object;
    private final @NotNull String help;

    private final boolean hasCommand;

    private Pattern command;

    public CommandMethod(@NotNull Method method, @NotNull Object object, @NotNull Command command)
    {
        this.method = method;
        this.object = object;
        this.help = command.help();

        this.hasCommand = !Utilities.isEmpty(command.command());

        if (this.hasCommand) {
            this.command = Pattern.compile(CommandManager.FormatCommand(command.command()));
        }
    }

    public void InvokeMethod(Object... args)
    {
        try {
            this.method.invoke(this.object, args);
        } catch (java.lang.IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            System.out.println("There was an error invoking the command method");
            e.printStackTrace();
        }
    }

    public @NotNull Method getMethod()
    {
        return this.method;
    }

    public @NotNull String getHelp() {
        return this.help;
    }

    public Pattern getCommand()
    {
        return this.command;
    }

    public boolean hasCommand()
    {
        return this.hasCommand;
    }

    public boolean hasParamaters() {
        return 0 != this.method.getParameterCount();
    }

    public boolean hasHelp() {
        return !Utilities.isEmpty(this.help);
    }
}
