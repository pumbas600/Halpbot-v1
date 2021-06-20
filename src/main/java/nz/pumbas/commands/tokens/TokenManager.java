package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.Order;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CustomParameter;
import nz.pumbas.commands.annotations.ParameterConstruction;
import nz.pumbas.commands.annotations.TokenSyntaxDefinition;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.exceptions.IllegalCustomParameterException;
import nz.pumbas.commands.exceptions.IllegalTokenSyntaxDefinitionException;
import nz.pumbas.commands.tokens.tokensyntax.CommandTokenInfo;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.commands.tokens.tokensyntax.TokenSyntaxDefinitions;
import nz.pumbas.commands.tokens.tokentypes.CommandToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.enums.Modifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * A static {@link CommandToken} manager, that handles the parsing of commands into {@link CommandToken command tokens}.
 */
public final class TokenManager {

    //TODO: Generate TokenCommand from array of parameter types for custom parameters if no constructor string is
    //      specified.

    private TokenManager() {}

    /**
     * A {@link Map} which maps custom classes to their parsed {@link Constructor} in an {@link TokenCommand}.
     */
    private static final Map<Class<?>, List<TokenCommand>> CustomClassConstructors = new HashMap<>();

    /**
     * A {@link Map} which maps custom parameter types in methods to a {@link BiFunction} that retrieves the value.
     */
    private static final Map<Class<?>, BiFunction<MessageReceivedEvent, AbstractCommandAdapter, Object>>
        CustomParameterMappings = Map.of(
            GenericEvent.class,           (e, a) -> e,
            AbstractCommandAdapter.class, (e, a) -> a,
            MessageChannel.class,         (e, a) -> e.getChannel(),
            User.class,                   (e, a) -> e.getAuthor(),
            ChannelType.class,            (e, a) -> e.getChannelType(),
            Guild.class,                  (e, a) -> e.getGuild()
    );

    /**
     * A {@link List} containing the registered {@link TokenSyntaxDefinition}.
     */
    private static final List<Method> RegisteredTokenSyntaxDefinitions = new ArrayList<>();

    static {
        registerTokenSyntax(TokenSyntaxDefinitions.class);
    }

    /**
     * @return A {@link Collection} of the supported custom parameter types.
     */
    public static Collection<Class<?>> getCustomParameterTypes()
    {
        return CustomParameterMappings.keySet();
    }

    /**
     * Retrieves the {@link BiFunction custom parameter mapper} for the specified {@link Class}. If the class isn't a
     * custom parameter, a function that always returns null is returned instead.
     *
     * @param customParameter
     *      The {@link Class} of the custom parameter
     *
     * @return The {@link BiFunction custom parameter mapper} for the specified {@link Class}
     */
    public static BiFunction<MessageReceivedEvent, AbstractCommandAdapter, Object> getCustomParameterMapper(
        @NotNull Class<?> customParameter)
    {
        return CustomParameterMappings.getOrDefault(customParameter, (e, a) -> new Object());
    }

    /**
     * Returns if the type is considered a build in type. This is determined by if the type is an enum or has a
     * defined type parser.
     *
     * @param type
     *      The {@link Class type} being checked
     *
     * @return If the type is a built in type
     */
    public static boolean isBuiltInType(Class<?> type)
    {
        type = Reflect.getPrimativeType(type);
        return type.isEnum() || Reflect.hasTypeParser(type);
    }

    /**
     * Registers all the static {@link Method} annotated with {@link TokenSyntaxDefinition}.
     *
     * @param tokenSyntaxDefinitions
     *      The {@link Class classes} containing the static {@link Method methods} annotated with
     *      {@link TokenSyntaxDefinition}
     */
    public static void registerTokenSyntax(Class<?>... tokenSyntaxDefinitions)
    {
        for (Class<?> tokenSyntaxDefinition : tokenSyntaxDefinitions) {
            //Add all the static methods which are annotated with TokenSyntaxDefinition
            List<Method> annotatedMethods = Reflect.getAnnotatedMethods(
                tokenSyntaxDefinition,TokenSyntaxDefinition.class, false);

            for (Method method : annotatedMethods) {
                if (!Reflect.hasModifiers(method, Modifiers.STATIC))
                    throw new IllegalTokenSyntaxDefinitionException(
                        String.format("The syntax token definition %s must be static", method.getName()));

                if (1 != method.getParameterCount() || !method.getParameterTypes()[0].isAssignableFrom(CommandTokenInfo.class))
                    throw new IllegalTokenSyntaxDefinitionException(
                        String.format("The syntax token definitions %s, must only take in TokenInfo as a parameter",
                            method.getName()));
            }

            RegisteredTokenSyntaxDefinitions.addAll(annotatedMethods);
        }

        //Re-sort the registered token syntax definitions based on the order defined within the TokenSyntaxDefinition
        //attribute.
        RegisteredTokenSyntaxDefinitions.sort(
            Comparator.comparing(m ->
                Reflect.getAnnotationFieldElse(m, TokenSyntaxDefinition.class,
                    TokenSyntaxDefinition::value, Order.NORMAL)));
    }

    /**
     * Retrieves the {@link List} of parsed {@link TokenCommand token commands} for the given {@link Class class's} constructors.
     * If the specified {@link Class} hasn't had their {@link Constructor constructors} parsed, then it calls {@link TokenManager#parseCustomClassConstructors(Class)}
     * and automatically returns the result.
     *
     * @param customClass
     *      The {@link Class} to retrieve the parsed {@link TokenCommand token commands} from.
     *
     * @return The {@link List} of {@link TokenCommand token commands} for the passed {@link Class}
     */
    public static List<TokenCommand> getParsedConstructors(Class<?> customClass)
    {
        if (!CustomClassConstructors.containsKey(customClass))
            parseCustomClassConstructors(customClass);
        return CustomClassConstructors.get(customClass);
    }

    /**
     * Generates the {@link CommandToken command tokens} for the {@link Constructor constructors} of the
     * {@link Class custom class} and adds it to {@link TokenManager#CustomClassConstructors}.
     *
     * @param customClass
     *      The {@link Class custom class} to parse the {@link Constructor constructors} for
     */
    public static void parseCustomClassConstructors(Class<?> customClass)
    {
        if (CustomClassConstructors.containsKey(customClass))
            return;

        if (isBuiltInType(customClass))
            throw new IllegalArgumentException(
                String.format("The class %s is a built in type.", customClass.getSimpleName()));

        Constructor<?>[] constructors = customClass.getDeclaredConstructors();
        if (0 == constructors.length)
            throw new IllegalCustomParameterException(
                String.format("The custom class %s, must define a constructor", customClass.getSimpleName()));

        List<Constructor<?>> customConstructors = Reflect.filterReflections(constructors,
            c -> c.isAnnotationPresent(ParameterConstruction.class));

        if (customConstructors.isEmpty()) {
            customConstructors.add(constructors[0]);
        }

        CustomClassConstructors.put(customClass,
                customConstructors
                        .stream()
                        .map(c -> new TokenCommand(null, c, parseConstructor(c))) //For constructors, an instance isn't required to invoke it.
                        .collect(Collectors.toList()));
    }

    /**
     * Generates the {@link CommandToken command tokens} for the specified {@link Constructor}.
     *
     * @param constructor
     *      The {@link Constructor} to generate the {@link CommandToken command tokens} for
     *
     * @return The generated {@link CommandToken command tokens}
     */
    private static List<CommandToken> parseConstructor(Constructor<?> constructor)
    {
        constructor.setAccessible(true);
        String constructorCommand = Reflect.getAnnotationFieldElse(constructor, ParameterConstruction.class,
            ParameterConstruction::constructor, "");

        if (constructorCommand.isEmpty())
            constructorCommand = generateCommand(constructor.getParameterTypes(), constructor.getParameterAnnotations());

        return parseCommand(constructorCommand,
            constructor.getParameterTypes(),
            constructor.getParameterAnnotations());
    }

    /**
     * Generates the {@link String command} from an array of {@link Class parameter types} and a 2D
     * array of {@link Annotation annotations}.
     *
     * @param parameterAnnotations
     *      A 2D array of {@link Annotation annotations}, with an array for each of the {@link Class} parameter types
     * @param parameterTypes
     *      The {@link Class parameter types} for the command {@link Method}
     *
     * @return The generating {@link String command}
     */
    public static String generateCommand(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
    {
        List<String> commandString = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
            Class<?> parameterType = parameterTypes[parameterIndex];

            if (Reflect.isAssignableTo(parameterType, CustomParameterMappings.keySet())) continue;

            String command = "#" + getTypeAlias(parameterType);

            if (Reflect.retrieveAnnotation(
                parameterAnnotations[parameterIndex], Unrequired.class).isPresent()) {
                command = "<" + command + ">";
            }

            commandString.add(command);
        }

        return String.join(" ", commandString);
    }

    /**
     * Generates an {@link TokenCommand} from the passed in {@link Object instance} and {@link Method}.
     *
     * @param instance
     *      The {@link Object} that the {@link Method} belongs to
     * @param method
     *      The {@link Method} to make the command from
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(Object instance, Method method)
    {

        List<Class<?>> reflections = method.isAnnotationPresent(Command.class)
            ? List.of(method.getAnnotation(Command.class).reflections())
            : Collections.emptyList();

        return generateTokenCommand(instance, method, reflections);
    }

    /**
     * Generates an {@link TokenCommand} from the passed in parameters.
     *
     * @param instance
     *      The {@link Object} that the {@link Method} belongs to
     * @param method
     *      The {@link Method} to make the command from
     * @param methodClasses
     *      The {@link Class classes} that the command can invoke methods from
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(Object instance, Method method, List<Class<?>> methodClasses)
    {
        return new TokenCommand(instance, method, parseCommand(method), methodClasses);
    }

    /**
     * Generates an {@link TokenCommand} from the passed in {@link Object instance} and {@link Method} and
     * {@link Command}.
     *
     * @param instance
     *      The {@link Object} that the {@link Method} belongs to
     * @param method
     *      The {@link Method} to make the command from
     * @param command
     *      The {@link Command} for this command
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(Object instance, Method method, Command command)
    {
        String displayCommand = retrieveDisplayCommand(method);

        return new TokenCommand(instance, method, parseCommand(method, displayCommand),
            displayCommand, command.description(), command.permission(), getRestrictedToList(command.restrictedTo()),
            List.of(command.reflections()));
    }

    /**
     * Retrieves a {@link List} of the ids of who can use this command.
     *
     * @param restrictedTo
     *      The array of user ids of who can use this command
     *
     * @return A {@link List} of the ids of who can use this command.
     */
    private static List<Long> getRestrictedToList(long[] restrictedTo)
    {
        List<Long> restrictedToList = new ArrayList<>();
        for (long user : restrictedTo)
            restrictedToList.add(user);

        return restrictedToList;
    }

    /**
     * Generates a {@link List} of {@link CommandToken command tokens} from the specified {@link Method}.
     *
     * @param method
     *      The {@link Method} to generate the {@link CommandToken command tokens} from
     *
     * @return A {@link List} of {@link CommandToken command tokens} representing the {@link Method}
     */
    public static List<CommandToken> parseCommand(Method method)
    {
        String displayCommand = retrieveDisplayCommand(method);
        return parseCommand(method, displayCommand);
    }

    /**
     * Generates a {@link List} of {@link CommandToken command tokens} from the specified {@link Executable}.
     *
     * @param executable
     *      The {@link Executable} to generate the {@link CommandToken command tokens} from
     * @param displayCommand
     *      The {@link String display command} for this method
     *
     * @return A {@link List} of {@link CommandToken command tokens} representing the {@link Executable}
     */
    public static List<CommandToken> parseCommand(Executable executable, String displayCommand)
    {
        return parseCommand(displayCommand,
            executable.getParameterTypes(),
            executable.getParameterAnnotations());
    }

    /**
     * Retrieves the {@link String display command} for the specified {@link Method}.
     *
     * @param method
     *      The {@link Method} to retrieve the display command for
     *
     * @return The {@link String} display command
     */
    public static String retrieveDisplayCommand(Method method)
    {
        Command command = method.getAnnotation(Command.class);
        return null == command || command.command().isEmpty()
            ? generateCommand(method.getParameterTypes(), method.getParameterAnnotations())
            : command.command();
    }

    /**
     * Generates a {@link List} of {@link CommandToken command tokens} from the specified information, which can be
     * automatically extracted from an {@link Method} by calling {@link TokenManager#parseCommand(Method)}.
     *
     * @param command
     *      The {@link String} which describes the syntax of the command
     * @param parameterTypes
     *      The {@link Class parameter types} corresponding to the {@link Method command method}
     * @param parameterAnnotations
     *      The {@link Annotation annotations} which correspond to each parameter
     *
     * @return A {@link List} of {@link CommandToken command tokens} representing the command
     */
    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes,
                                                  Annotation[][] parameterAnnotations)
    {
        List<String> tokens = splitCommandTokens(command);

        return parseCommandTokens(tokens, parameterTypes,
            parameterAnnotations);
    }

    public static List<String> splitCommandTokens(@NotNull String command)
    {
        List<String> tokens = new ArrayList<>();

        if (command.isEmpty())
            return tokens;

        String[] temporaryTokens = command.split(" ");

        //Converts <my name is> to: <my> <name> <is> when splitting the command into tokens.
        boolean isOptional = false;
        for (String token : temporaryTokens) {
            if (isOptional) {
                if (!token.startsWith("<"))
                    token = '<' + token;
                if (!token.endsWith(">"))
                    token += '>';
                else
                    isOptional = false;
            }
            else if (token.startsWith("<") && !token.endsWith(">")) {
                isOptional = true;
                token += '>';
            }
            tokens.add(token);
        }

        return tokens;
    }

    /**
     * Parses the {@link List} of {@link String command tokens} into a {@link List<CommandToken>} by calling on the
     * registered {@link TokenSyntaxDefinition token syntax definitions}.
     *
     * @param tokens
     *      The {@link List<String> command tokens} which define the {@link Command}
     * @param parameterTypes
     *      The {@link Class parameter types} of the {@link Method}
     * @param parameterAnnotations
     *      The {@link Annotation annotations} for the parameters in the {@link Method}
     *
     * @return The parsed {@link List<CommandToken>}
     */
    private static List<CommandToken> parseCommandTokens(List<String> tokens, Class<?>[] parameterTypes,
                                                         Annotation[][] parameterAnnotations)
    {
        CommandTokenInfo commandTokenInfo = new CommandTokenInfo(tokens, parameterTypes, parameterAnnotations);
        List<CommandToken> commandTokens = new ArrayList<>();

        while (commandTokenInfo.hasToken()) {
            for (Method tokenSyntaxDefinition : RegisteredTokenSyntaxDefinitions) {
                try {
                    Object result = tokenSyntaxDefinition.invoke(null, commandTokenInfo);
                    if (result instanceof CommandToken) {
                        if (result instanceof ParsingToken) {
                            commandTokenInfo.incrementParameterIndex();
                        }
                        commandTokens.add((CommandToken) result);
                        break;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    ErrorManager.handle(e);
                }
            }

            commandTokenInfo.nextToken();
        }

        return commandTokens;
    }

    /**
     * Retrieves an {@link Optional} containing the result of invoking the matching method.
     *
     * @param invocationToken
     *      The {@link InvocationTokenInfo}
     * @param reflections
     *      The {@link List<Class>} of classes that methods are allowed to be invoked from
     * @param requiredReturnType
     *      The required return type of the method
     *
     * @return An {@link Optional} containing the invoked matching method.
     */
    public static Optional<Object> getTokenCommandFromMethodInvocation(@NotNull InvocationTokenInfo invocationToken,
                                                                       @NotNull List<Class<?>> reflections,
                                                                       @NotNull Class<?> requiredReturnType)
    {
        if (reflections.isEmpty()) return Optional.empty();

        Optional<String> oType = invocationToken.getNextSurrounded("#", ".");
        if (oType.isEmpty()) return Optional.empty();


        Optional<Class<?>> oMethodClass = reflections
            .stream()
            .filter(c -> TokenManager.getTypeAlias(c).equalsIgnoreCase(oType.get()))
            .findFirst();

        if (oMethodClass.isPresent()) {
            Class<?> methodClass = oMethodClass.get();

            Optional<String> oMethodName = invocationToken.getNext("(", false);
            Optional<String> oParameters = invocationToken.getNextSurrounded("(", ")");

            if (oMethodName.isPresent() && oParameters.isPresent()) {
                String methodName = oMethodName.get();
                InvocationTokenInfo parameters = InvocationTokenInfo.of(oParameters.get()).saveState(invocationToken);

                return Reflect.getMethods(methodClass, methodName, true)
                    .stream()
                    .filter(m -> Reflect.hasModifiers(m, Modifiers.PUBLIC, Modifiers.STATIC))
                    .filter(m -> requiredReturnType.isAssignableFrom(m.getReturnType()))
                    .map(m -> TokenManager.generateTokenCommand(null, m, reflections))
                    .filter(m -> m.matches(parameters.restoreState(invocationToken)))
                    .findFirst()
                    .flatMap(tokenCommand -> tokenCommand.invoke(parameters.restoreState(invocationToken), null, null));
            }
        }

        return Optional.empty();
    }

    /**
     * Returns if the {@link String parsing token} matches the required {@link Class type} of the method parameter. This will check the {@link Class} for an {@link CustomParameter}
     * and use the specified identifier if present, otherwise, it will check that the {@link Class type's} name matches the {@link String parsing token}.
     *
     * @param token
     *      The {@link String} representation of an {@link ParsingToken}, in the format #type
     *
     * @param type
     *      The required {@link Class type} of the {@link ParsingToken}
     *
     * @return if the {@link String parsing token} is valid
     */
    public static boolean isValidCommandTypeToken(String token, Class<?> type) {
        @NonNls String tokenIdentifier = token.substring(1);
        return token.startsWith("#") && getTypeAlias(type).equalsIgnoreCase(tokenIdentifier);
    }

    /**
     * Gets the {@link String alias} of the specified {@link Class type} by first checking if it has the {@link CustomParameter} annotation. If it does, it
     * retrieves the specified {@link String identifier}, otherwise it just gets the name of the {@link Class type}.
     *
     * @param type
     *      The {@link Class type} to get the alias of
     *
     * @return The {@link String alias} of the specified {@link Class type}
     */
    public static String getTypeAlias(Class<?> type)
    {
        String defaultAlias = type.isArray()
            ? Reflect.getPrimativeType(Reflect.getArrayType(type)).getSimpleName() + "[]"
            : Reflect.getPrimativeType(type).getSimpleName();

        return type.isAnnotationPresent(CustomParameter.class)
                ? type.getAnnotation(CustomParameter.class).identifier()
                : defaultAlias;
    }
}
