package nz.pumbas.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import nz.pumbas.commands.Annotations.Unrequired;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.commands.Annotations.ParameterConstruction;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.commands.Exceptions.IllegalCustomParameterException;
import nz.pumbas.utilities.Utilities;

public final class CommandManager extends ListenerAdapter
{
    //TODO: Unit Manager
    //TODO: varargs
    //TODO: Optional arguments -> Using @Optional annotation - Need to take this into consideration when automatically generating command
    //TODO: Flags
    //TODO: Manually submit type parsers (E.g: For types you don't have access to)?

    public static final Map<String, String> CommandRegex = Map.of(
        "SENTENCE", "([\\w ,\\.!\\?]+)"
    );

    public static final Map<Class<?>, CommandType> CommandTypes = Map.of(
        String.class, new CommandType(String.class, "WORD", "([\\w]+)", s -> s),
        int.class, new CommandType(int.class, "INT", "(-?\\d+)", Integer::parseInt),
        float.class, new CommandType(float.class, "FLOAT", "(-?\\d+\\.?\\d*)", Float::parseFloat),
        double.class, new CommandType(double.class, "DOUBLE", "(-?\\d+\\.?\\d*)", Double::parseDouble),
        char.class, new CommandType(char.class, "CHAR", "([a-zA-Z])", s -> s.charAt(0))
    );

    private final Map<Class<?>, CustomParameterType> customParameterTypes;
    private final Map<String, List<CommandMethod>> registeredCommands;

    public CommandManager()
    {
        this.customParameterTypes = new HashMap<>();
        this.registeredCommands = new HashMap<>();
    }

    public CommandManager(@NotNull JDABuilder builder)
    {
        this.customParameterTypes = new HashMap<>();
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
                        //Only optionals (So there's the possibility of no text after the alias)
                        if (this.handleCommandMethodRegexCall(command, event, "")) {
                            return;
                        }
                    } else if (!"?".equals(splitText[1])){
                        String content = splitText[1];

                        if (this.handleCommandMethodRegexCall(command, event, content))
                            return;
                    }
                } catch (InvocationTargetException e) {
                    ErrorManager.handle(event, e.getTargetException());
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

            this.parseValues(command.getMethod().getParameterAnnotations(),
                parameterTypes, command.getConstructors(), 0,
                    parameters, 1,
                    matcher, 1);
            command.InvokeMethod(parameters);
            return true;
        }
        return false;
    }

    private int[] handleCustomParameterType(CustomParameterType customParameter,
                                            List<Constructor<?>> customConstructors, int constructorIndex,
                                            Object[] parameters, int parameterIndex,
                                            Matcher matcher, int groupIndex)
    {
        Constructor<?> constructor = customConstructors.get(constructorIndex);
        constructorIndex++;

        Object[] constructorParameters = new Object[constructor.getParameterCount()];
        int[] result = this.parseValues(constructor.getParameterAnnotations(),
            constructor.getParameterTypes(),
            customConstructors, constructorIndex,
            constructorParameters,0,
            matcher,groupIndex);
        constructorIndex = result[0];
        groupIndex = result[1];

        try {
            parameters[parameterIndex] = constructor.newInstance(constructorParameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ErrorManager.handle(e, String.format("There was an error trying to create an instance of %s",
                customParameter.getType().getSimpleName()));
        }

        return new int[] { constructorIndex, groupIndex} ;
    }

    @SuppressWarnings("unchecked")
    private int[] parseValues(Annotation[][] parameterAnnotations,
                              Class<?>[] parameterTypes,
                              List<Constructor<?>> customConstructors, int constructorIndex,
                              Object[] parameters, int startParameterIndex,
                              Matcher matcher, int groupIndex)
    {
        for (int parameterIndex = startParameterIndex; parameterIndex < parameterTypes.length; parameterIndex++) {
            String match = matcher.group(groupIndex);
            Class<?> currentParameter = parameterTypes[parameterIndex];

            if (null == match) {
                Optional<Unrequired> oUnrequired =
                    Utilities.retrieveAnnotation(parameterAnnotations[parameterIndex], Unrequired.class);
                if (oUnrequired.isPresent()) {
                    Unrequired unrequired = oUnrequired.get();
                    if (!unrequired.value().isEmpty())
                        match = unrequired.value();
                }
                else {
                    parameters[parameterIndex] = null;
                    groupIndex++;
                    continue;
                }
            }
            if (currentParameter.isEnum()) {
                parameters[parameterIndex] = Enum.valueOf((Class<? extends Enum>)currentParameter,match.toUpperCase());
                groupIndex++;
            }
            else if (CommandTypes.containsKey(currentParameter)) {
                parameters[parameterIndex] = CommandTypes.get(currentParameter).getTypeParser().apply(match);
                groupIndex++;
            }
            else if (this.customParameterTypes.containsKey(currentParameter)) {
                CustomParameterType customParameter = this.customParameterTypes.get(currentParameter);
                int[] result = this.handleCustomParameterType(customParameter,
                    customConstructors,constructorIndex,
                    parameters,parameterIndex,
                    matcher,groupIndex);
                constructorIndex = result[0];
                groupIndex = result[1];
            }
        }
        return new int[] { constructorIndex, groupIndex };
    }

    private EmbedBuilder buildHelpMessage(EmbedBuilder builder, String commandAlias)
    {
        builder.addField(Utilities.line, String.format("**Command:**    %s", commandAlias), false);

        //Couldn't find a command that matched
        this.registeredCommands.get(commandAlias).forEach(c -> {
            String description = c.hasDescription() ? c.getDescription() : "This command doesn't have a description.";
            String parameters = c.hasCommand() ? c.getDisplayCommand() : "This command doesn't have any parameters.";
            builder.addField("", String.format("**Parameters:** %s\n**Regex:** %s\n**Description:** %s", parameters,
                c.getCommand().pattern(), description), false);
        });

        return builder;
    }

    private void registerCommandMethods(List<Method> methods, Object object)
    {
        String defaultPrefix = Utilities.getAnnotationFieldElse(
            object.getClass(), CommandGroup.class, CommandGroup::defaultPrefix, "");
        boolean hasDefaultPrefix = !defaultPrefix.isEmpty();

        for (Method method : methods) {
            method.setAccessible(true);
            Command annotation = method.getAnnotation(Command.class);
            boolean hasPrefix = !annotation.prefix().isEmpty();

            if (!hasDefaultPrefix && !hasPrefix)
                throw new IllegalCommandException(
                    String.format("There's no default prefix for the method %s, so the command must define one.", method.getName()));

            String commandAlias = hasPrefix ? annotation.prefix() : defaultPrefix + annotation.alias();

            this.createCommandMethods(method, object, annotation).forEach(commandMethod -> {
                if (this.registeredCommands.containsKey(commandAlias))
                    this.registeredCommands.get(commandAlias).add(commandMethod);
                else {
                    List<CommandMethod> commandMethods = new ArrayList<>();
                    commandMethods.add(commandMethod);
                    this.registeredCommands.put(commandAlias, commandMethods);
                }
            });
        }
    }

    private List<CommandMethod> createCommandMethods(Method method, Object object, Command commandAnnotation) {
        boolean hasCommand = 1 < method.getParameterCount();

        if (hasCommand) {
            this.checkForCustomTypes(method.getParameterTypes());

            String command = commandAnnotation.command().isEmpty()
                ? this.automaticallyGenerateCommand(method.getParameterAnnotations(), method.getParameterTypes())
                : commandAnnotation.command();

            return this.formatCommand(method.getParameterTypes(), command).map(commandInfo ->
                new CommandMethod(method, object, commandAnnotation, hasCommand,
                    commandInfo.displayCommand, Pattern.compile(commandInfo.regexCommand), commandInfo.constructors))
                .collect(Collectors.toList());
        }
        return List.of(
            new CommandMethod(method, object, commandAnnotation,
                hasCommand, null, null, null));
    }

    private Stream<CommandInfo> formatCommand(Class<?>[] parameterTypes, String command)
    {
        return this.generateCommandInfo(parameterTypes,command)
            .stream()
            .peek(this::formatCommandInfo);
    }

    private void formatCommandInfo(CommandInfo commandInfo) {
        //For SENTENCE
        for (String key : CommandRegex.keySet()) {
            commandInfo.regexCommand = commandInfo.regexCommand.replace(key, CommandRegex.get(key));
        }

        for (CommandType commandType : CommandTypes.values()) {
            commandInfo.regexCommand = commandInfo.regexCommand.replace(commandType.getAlias(), commandType.getCommand());
        }

        //Turns the optional non-capture formatting <...> into REGEX
        int openNonClosingIndex;
        while (-1 != (openNonClosingIndex = commandInfo.regexCommand.indexOf('<'))) {
            int closeNonCapturingIndex = commandInfo.regexCommand.indexOf('>');
            if (-1 == closeNonCapturingIndex)
                throw new IllegalCommandException(
                    String.format("Unclosed non-capturing group. Open '<' at %s", openNonClosingIndex));

            boolean spaceBefore
                = 0 != openNonClosingIndex && ' ' == commandInfo.regexCommand.charAt(openNonClosingIndex - 1);
            boolean spaceAfter
                = commandInfo.regexCommand.length() -1 > closeNonCapturingIndex && ' ' == commandInfo.regexCommand.charAt(closeNonCapturingIndex + 1);

            commandInfo.regexCommand = (spaceBefore && spaceAfter)
                    ? Utilities.replaceFirst(commandInfo.regexCommand, "> ", ")* ?")
                    : Utilities.replaceFirst(commandInfo.regexCommand, ">", ")*");

            commandInfo.regexCommand = (spaceBefore && !spaceAfter)
                    ? Utilities.replaceFirst(commandInfo.regexCommand, " <", " ?(?:")
                    : Utilities.replaceFirst(commandInfo.regexCommand, "<", "(?:");
        }

        commandInfo.regexCommand = "^" + commandInfo.regexCommand;
    }

    //I barely understand whats happening here, so this will definitely need to be reworked at some point...
    private List<CommandInfo> generateCommandInfo(Class<?>[] parameterTypes,
                                                  String command) {
        List<CommandInfo> results = new ArrayList<>();
        results.add(new CommandInfo(command, command));
        for (Class<?> parameterType : parameterTypes) {
            if (!this.customParameterTypes.containsKey(parameterType)) continue;

            CustomParameterType customParameter = this.customParameterTypes.get(parameterType);
            List<CommandInfo> resultCopy = new ArrayList<>(results);
            List<CommandInfo> tempResults = new ArrayList<>();
            for (ConstructorPair constructorPair : customParameter.getTypeConstructors()) {
                this.generateCommandInfo(constructorPair.constructor.getParameterTypes(), constructorPair.constructorRegex)
                    .forEach(ci ->
                        resultCopy.forEach(resultCI-> {
                            CommandInfo commandInfo = new CommandInfo(
                                Utilities.replaceFirst(resultCI.regexCommand, customParameter.getTypeAlias(), ci.regexCommand),
                                Utilities.replaceFirst(resultCI.displayCommand, customParameter.getTypeAlias(),
                                    String.format("%s[%s] ", Utilities.capitalise(customParameter.getTypeAlias()),
                                        ci.displayCommand)));
                            commandInfo.constructors.addAll(resultCI.constructors);
                            commandInfo.constructors.add(constructorPair.constructor);
                            commandInfo.constructors.addAll(ci.constructors);
                            tempResults.add(commandInfo);
                        })
                    );
            }
            results = tempResults;
        }
        return results;
    }

    private void registerCustomParameterType(Class<?>... customParameters)
    {
        for (Class<?> clazz : customParameters) {
            if (0 == clazz.getDeclaredConstructors().length)
                throw new IllegalCustomParameterException(
                    String.format("The custom parameter type %s, must define a constructor", clazz.getSimpleName()));

            List<ConstructorPair> constructors = new ArrayList<>();
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (!constructor.isAnnotationPresent(ParameterConstruction.class)) continue;

                ParameterConstruction parameterConstruction = constructor.getAnnotation(ParameterConstruction.class);
                constructor.setAccessible(true);

                String constructorRegex = parameterConstruction.constructor().isEmpty()
                    ? this.automaticallyGenerateCommand(constructor.getParameterAnnotations(),
                                                        constructor.getParameterTypes())
                    : parameterConstruction.constructor();

                constructors.add(new ConstructorPair(constructorRegex, constructor));
            }

            if (constructors.isEmpty()) continue;

            CustomParameterType customParameterType = new CustomParameterType(clazz, constructors);
            this.customParameterTypes.put(clazz, customParameterType);
        }
    }

    public String automaticallyGenerateCommand(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) {
        List<String> constructorString = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
            Class<?> parameterType = parameterTypes[parameterIndex];

            if (parameterType.isAssignableFrom(MessageReceivedEvent.class)) continue;

            String command = null;
            if (parameterType.isEnum()) {
                command = CommandManager.CommandTypes.get(String.class).getAlias();
            }
            else if (CommandManager.CommandTypes.containsKey(parameterType)) {
                command = CommandManager.CommandTypes.get(parameterType).getAlias();
            }
            else if (this.customParameterTypes.containsKey(parameterType)) {
                command = this.customParameterTypes.get(parameterType).getTypeAlias();
            }

            if (null != command &&
                Utilities.retrieveAnnotation(parameterAnnotations[parameterIndex], Unrequired.class)
                    .isPresent()) {
                command = "<" + command + ">";
            }

            constructorString.add(command);
        }

        return String.join(" ", constructorString);
    }

    private void checkForCustomTypes(Class<?>[] parameterTypes) {
        for (Class<?> parameterType : parameterTypes) {
            boolean isRegisteredType =
                parameterType.isAssignableFrom(MessageReceivedEvent.class) ||
                parameterType.isEnum() ||
                CommandManager.CommandTypes.containsKey(parameterType) ||
                this.customParameterTypes.containsKey(parameterType);

            if (!isRegisteredType) {
                this.registerCustomParameterType(parameterType);
            }
        }
    }
}
