package nz.pumbas.commands;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.utilities.Utilities;

public final class CommandManager extends ListenerAdapter
{
    private static final Map<Class<?>, Function<String, Object>> TypeParsers = Map.of(
        String.class, s -> s,
        int.class, Integer::parseInt,
        float.class, Float::parseFloat
    );

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

        String[] splitText = event.getMessage().getContentRaw().split(" ", 1);
        String commandAlias = splitText[0];

        if (this.registeredCommands.containsKey(commandAlias)) {
            CommandMethod command = this.registeredCommands.get(commandAlias);

            if (!command.hasParamaters()) {
                command.InvokeMethod();
                return;
            }

            if (!command.hasCommand()) {
                command.InvokeMethod(event);
                return;
            }

            String content = splitText[1];

            Matcher matcher = command.getCommand().matcher(content);
            if (matcher.matches()) {
                Class<?>[] parameters = command.getMethod().getParameterTypes();
                Object[] args = new Object[parameters.length];
                args[0] = event;

                for (int i = 1; i < Math.min(matcher.groupCount(), parameters.length); i++) {
                    String match = matcher.group(i);
                    Object argValue = null;

                    if (null != match && TypeParsers.containsKey(parameters[i])) {
                        argValue = TypeParsers.get(parameters[i]).apply(match);
                    }
                    args[i] = argValue;
                }

                command.InvokeMethod(args);
                return;
            }

            String helpMessage = String.format(
                    "HALP: %s\n-----------------------------------\n%s",
                    commandAlias, command.hasHelp() ? command.getHelp() : "Your command doesn't seem to have been formated correctly."
            );

            event.getChannel().sendMessage(helpMessage).queue();
        }
    }

    private void registerCommandMethods(List<Method> methods, Object object)
    {
        String defaultPrefix = Utilities.getAnnotationFieldElse(
                object, CommandGroup.class, CommandGroup::defaultPrefix, "");
        boolean hasDefaultPrefix = !Utilities.isEmpty(defaultPrefix);

        for (Method method : methods) {
            Command annotation = method.getAnnotation(Command.class);
            boolean hasPrefix = !Utilities.isEmpty(annotation.prefix());

            if (!hasDefaultPrefix && !hasPrefix)
                throw new IllegalCommandException(
                        String.format("There's no default prefix for the method %s, so the command must define one.", method.getName()));

            String commandAlias = hasPrefix ? annotation.prefix() : defaultPrefix + annotation.alias();

            if (this.registeredCommands.containsKey(commandAlias))
                throw new IllegalCommandException(
                        String.format("There is already a command registered with the alias %s.", commandAlias));

            this.registeredCommands.put(commandAlias, new CommandMethod(method, object, annotation));
        }
    }

}
