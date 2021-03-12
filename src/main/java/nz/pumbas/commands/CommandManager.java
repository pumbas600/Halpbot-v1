package nz.pumbas.commands;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.utilities.Utilities;

public final class CommandManager extends ListenerAdapter
{

    private final Map<String, CommandMethod> registeredCommands;

    public CommandManager(JDABuilder builder)
    {
        this.registeredCommands = new HashMap<>();
        builder.addEventListeners(this);
    }

    public CommandManager registerCommands(Object... objects)
    {
        for (Object object : objects) {
            this.registerCommandMethods(Utilities.getAnnotatedMethods(
                    object.getClass(), Command.class, false),
                    object);
        }
        return this;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;

        String commandAlias = event.getMessage().getContentRaw().split(" ", 1)[0];

        if (this.registeredCommands.containsKey(commandAlias)) {
            this.registeredCommands.get(commandAlias).InvokeMethod(event);
        }
    }

    private void registerCommandMethods(List<Method> methods, Object object)
    {
        String defaultPrefix = Utilities.getAnnotationFieldElse(
                object, CommandGroup.class, CommandGroup::defaultPrefix, "");
        boolean hasDefaultPrefix = hasPrefix(defaultPrefix);

        for (Method method : methods) {
            if (!(0 == method.getParameterCount() ||
                    (1 == method.getParameterCount() && MessageReceivedEvent.class != method.getParameterTypes()[0])))
                throw new IllegalCommandException("A command should only take the one parameter, MessageReceivedEvent");

            Command annotation = method.getAnnotation(Command.class);
            boolean hasPrefix = hasPrefix(annotation.prefix());

            if (!hasDefaultPrefix && !hasPrefix)
                throw new IllegalCommandException(
                        String.format("There's no default prefix for the method %s, so the command must define one.", method.getName()));

            String commandAlias = hasPrefix ? annotation.prefix() : defaultPrefix + annotation.alias();

            if (this.registeredCommands.containsKey(commandAlias))
                throw new IllegalCommandException(
                        String.format("There is already a command registered with the alias %s.", commandAlias));

            this.registeredCommands.put(commandAlias, new CommandMethod(method, object));
        }
    }

    private static boolean hasPrefix(String prefix)
    {
        return !"".equals(prefix);
    }

}
