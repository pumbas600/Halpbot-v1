package nz.pumbas.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Annotations.CustomParameter;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.commands.Exceptions.IllegalCustomParameterException;
import nz.pumbas.commands.Exceptions.UnimplementedFeatureException;
import nz.pumbas.utilities.Utilities;

public final class CommandManager extends ListenerAdapter
{
    //TODO: Unit Manager
    //TODO: varargs
    //TODO: Optional arguments

    public static final Map<String, String> CommandRegex = Map.of(
        "SENTENCE", "([a-zA-Z ]+)"
    );

    public static final Map<Class<?>, CommandType> CommandTypes = Map.of(
        String.class, new CommandType(String.class, "WORD", "([a-zA-Z]+)", s -> s),
        int.class, new CommandType(int.class, "INT", "(-?\\d+)", Integer::parseInt),
        float.class, new CommandType(float.class, "FLOAT", "(-?\\d+\\.?\\d*)", Float::parseFloat),
        double.class, new CommandType(double.class, "DOUBLE", "(-?\\d+\\.?\\d*)", Double::parseDouble),
        char.class, new CommandType(char.class, "CHAR", "([a-zA-Z])", s -> s.charAt(0))
    );

    private final Map<Class<?>, CustomParameterType> customParameterTypes;
    private final Map<String, List<CommandMethod>> registeredCommands;

    public CommandManager(JDABuilder builder, Class<?>... customParameters)
    {
        this.customParameterTypes = new HashMap<>();
        this.registeredCommands = new HashMap<>();

        builder.addEventListeners(this);
        this.registerCustomParameterType(customParameters);
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

        String[] splitText = event.getMessage().getContentRaw().split(" ", 2);
        String commandAlias = splitText[0];

        if (this.registeredCommands.containsKey(commandAlias)) {
            for (CommandMethod command : this.registeredCommands.get(commandAlias)) {
                try {
                    if (1 == splitText.length) {
                        if (!command.hasParamaters()) {
                            command.InvokeMethod();
                            return;
                        }

                        if (!command.hasCommand()) {
                            command.InvokeMethod(event);
                            return;
                        }
                    } else {
                        String content = splitText[1];

                        if (this.handleCommandMethodRegexCall(command, event, content))
                            return;
                    }
                } catch (InvocationTargetException e) {
                    handle(event, e.getTargetException());
                    return;
                }
            }
            //Couldn't find a matching command
            event.getChannel().sendMessage(
                this.buildHelpMessage(
                    new EmbedBuilder().setColor(Color.cyan).setTitle("HALP"), commandAlias)
                    .build())
                .queue();
        }
    }

    private boolean handleCommandMethodRegexCall(CommandMethod command, MessageReceivedEvent event, String commandContent)
        throws InvocationTargetException
    {
        Matcher matcher = command.getCommand().matcher(commandContent);
        if (matcher.lookingAt()) {
            Class<?>[] parameterTypes = command.getMethod().getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];
            parameters[0] = event;

            this.parseValues(parameterTypes, parameters, 1, matcher, 1);
            command.InvokeMethod(parameters);
            return true;
        }
        return false;
    }

    private int handleCustomParameterType(CustomParameterType customParameter, Object[] parameters, int parameterIndex, Matcher matcher, int groupIndex)
    {
        Constructor<?> constructor = customParameter.getType().getDeclaredConstructors()[0];

        Object[] constructorParameters = new Object[constructor.getParameterCount()];
        groupIndex = this.parseValues(constructor.getParameterTypes(), constructorParameters, 0, matcher, groupIndex);

        try {
            parameters[parameterIndex] = constructor.newInstance(constructorParameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            handle(e, String.format("There was an error trying to create an instance of %s",
                customParameter.getType().getSimpleName()));
        }

        return groupIndex;
    }

    private int parseValues(Class<?>[] parameterTypes, Object[] parameters, int startParameterIndex, Matcher matcher, int groupIndex)
    {
        for (int parameterIndex = startParameterIndex; parameterIndex < parameterTypes.length; parameterIndex++) {
            String match = matcher.group(groupIndex);
            Class<?> currentParameter = parameterTypes[parameterIndex];

            if (CommandTypes.containsKey(currentParameter)) {
                parameters[parameterIndex] = CommandTypes.get(currentParameter).getTypeParser().apply(match);
                groupIndex++;
            } else if (this.customParameterTypes.containsKey(currentParameter)) {
                CustomParameterType customParameter = this.customParameterTypes.get(currentParameter);
                groupIndex = this.handleCustomParameterType(customParameter, parameters, parameterIndex, matcher, groupIndex);
            }
        }
        return groupIndex;
    }

    private EmbedBuilder buildHelpMessage(EmbedBuilder builder, String commandAlias)
    {
        builder.addField("Command", commandAlias, true);

        //Couldn't find a command that matched
        this.registeredCommands.get(commandAlias).forEach(c -> {
            String description = c.hasDescription() ? c.getDescription() : "This command doesn't have a description.";
            builder.addField("Description", description, false);
        });

        return builder;
    }

    private void registerCommandMethods(List<Method> methods, Object object)
    {
        String defaultPrefix = Utilities.getAnnotationFieldElse(
            object, CommandGroup.class, CommandGroup::defaultPrefix, "");
        boolean hasDefaultPrefix = !Utilities.isEmpty(defaultPrefix);

        for (Method method : methods) {
            method.setAccessible(true);
            Command annotation = method.getAnnotation(Command.class);
            boolean hasPrefix = !Utilities.isEmpty(annotation.prefix());

            if (!hasDefaultPrefix && !hasPrefix)
                throw new IllegalCommandException(
                    String.format("There's no default prefix for the method %s, so the command must define one.", method.getName()));

            String commandAlias = hasPrefix ? annotation.prefix() : defaultPrefix + annotation.alias();

            CommandMethod commandMethod = this.createCommandMethod(method, object, annotation);
            if (this.registeredCommands.containsKey(commandAlias))
                this.registeredCommands.get(commandAlias).add(commandMethod);
            else
                this.registeredCommands.put(commandAlias, List.of(commandMethod));
        }
    }

    private CommandMethod createCommandMethod(Method method, Object object, Command commandAnnotation) {
        boolean hasCommand = 1 < method.getParameterCount();

        Pattern commandRegex = null;
        if (hasCommand) {
            String command = Utilities.isEmpty(commandAnnotation.command())
                ? this.automaticallyGenerateCommand(method.getParameterTypes())
                : commandAnnotation.command();

            commandRegex = Pattern.compile(this.formatCommand(command));
        }
        return new CommandMethod(method, object, commandAnnotation, hasCommand, commandRegex);
    }

    public String formatCommand(String command)
    {
        command = this.formatCustomTypes(command);

        //For SENTENCE
        for (String key : CommandRegex.keySet()) {
            command = command.replace(key, CommandRegex.get(key));
        }

        for (CommandType commandType : CommandTypes.values()) {
            command = command.replace(commandType.getAlias(), commandType.getCommand());
        }

        //Turns the optional non-capture formatting <...> into REGEX
        int noncaptureIndex;
        while (-1 != (noncaptureIndex = command.indexOf('<'))) {

            if (0 != noncaptureIndex && ' ' == command.charAt(noncaptureIndex - 1)) {
                command = Utilities.replaceFirst(command, "> ", ")? ?");
            } else {
                command = Utilities.replaceFirst(command, ">", ")?");
            }
            command = Utilities.replaceFirst(command, "<", "(?:");
        }

        return "^" + command;
    }

    private String formatCustomTypes(String command)
    {
        for (CustomParameterType customParameter : this.customParameterTypes.values()) {
            if (command.contains(customParameter.getTypeAlias())) {
                command = command.replace(customParameter.getTypeAlias(),
                    this.formatCustomTypes(customParameter.getTypeConstructor()));
            }
        }
        return command;
    }

    private void registerCustomParameterType(Class<?>... customParameters)
    {
        for (Class<?> clazz : customParameters) {
            if (clazz.isAnnotationPresent(CustomParameter.class)) {
                CustomParameter customParameter = clazz.getAnnotation(CustomParameter.class);

                if (0 == clazz.getDeclaredConstructors().length)
                    throw new IllegalCustomParameterException(
                        String.format("The custom parameter type %s, must define a constructor", clazz.getSimpleName()));

                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                constructor.setAccessible(true);

                String constructorString = Utilities.isEmpty(customParameter.constructor())
                    ? this.automaticallyGenerateCommand(constructor.getParameterTypes())
                    : customParameter.constructor();

                CustomParameterType customParameterType = new CustomParameterType(clazz, customParameter,
                    constructorString);
                this.customParameterTypes.put(clazz, customParameterType);
            }

        }
    }

    public String automaticallyGenerateCommand(Class<?>[] parameterTypes) {
        List<String> constructorString = new ArrayList<>();
        for (Class<?> parameterType : parameterTypes) {
            //These are manually passed and don't need to be included in the command
            if (parameterType.isAssignableFrom(MessageReceivedEvent.class))
                continue;

            if (CommandManager.CommandTypes.containsKey(parameterType)) {
                constructorString.add(CommandManager.CommandTypes.get(parameterType).getAlias());
            }
            else if (this.customParameterTypes.containsKey(parameterType)) {
                constructorString.add(this.customParameterTypes.get(parameterType).getTypeAlias());
            }
        }

        return String.join(" ", constructorString);
    }

    public static void handle(Throwable e)
    {
        handle(null, e, null);
    }

    public static void handle(MessageReceivedEvent event, Throwable e)
    {
        handle(event, e, null);
    }

    public static void handle(Throwable e, String message)
    {
        handle(null, e, message);
    }

    public static void handle(MessageReceivedEvent event, Throwable e, String message)
    {
        if (null != message)
            System.out.println(message);

        if (e instanceof UnimplementedFeatureException) {
            unimplementedFeatureEmbed(event, e.getMessage());
        } else e.printStackTrace();
    }

    public static void unimplementedFeatureEmbed(MessageReceivedEvent event, String message)
    {
        event.getChannel().sendMessage(
            new EmbedBuilder().setTitle(":confounded: Sorry...")
                .setColor(Color.red)
                .addField("This feature is not implemented yet", message, false)
                .build())
            .queue();

    }
}
