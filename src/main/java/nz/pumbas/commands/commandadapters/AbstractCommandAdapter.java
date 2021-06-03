package nz.pumbas.commands.commandadapters;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.utilities.Reflect;

public abstract class AbstractCommandAdapter extends ListenerAdapter
{
    protected final Map<String, CommandMethod> registeredCommands = new HashMap<>();

    protected AbstractCommandAdapter(@Nullable JDABuilder builder)
    {
        builder.addEventListeners(this);
    }

    public AbstractCommandAdapter registerCommands(Object... instances)
    {
        for (Object instance : instances) {
            this.registerCommandMethods(instance, Reflect.getAnnotatedMethods(
                instance.getClass(), Command.class, false));
        }
        return this;
    }

    protected abstract void registerCommandMethods(Object instance, List<Method> annotatedMethods);
}
