package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.Order;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CustomParameter;
import nz.pumbas.commands.annotations.ParameterConstruction;
import nz.pumbas.commands.annotations.TokenSyntaxDefinition;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.exceptions.IllegalCommandException;
import nz.pumbas.commands.exceptions.IllegalCustomParameterException;
import nz.pumbas.commands.exceptions.IllegalTokenSyntaxDefinitionException;
import nz.pumbas.commands.tokens.tokensyntax.CommandTokenInfo;
import nz.pumbas.commands.tokens.tokensyntax.TokenSyntaxDefinitions;
import nz.pumbas.commands.tokens.tokentypes.CommandToken;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.enums.Modifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A static {@link CommandToken} manager, that handles the parsing of commands into {@link CommandToken command tokens}.
 */
public final class TokenManager {

    //TODO: NTS - InvocationTokenInfo should be checked hasNext before being passed to method.

    private TokenManager() {}

    /**
     * A {@link Map} which maps custom classes to their parsed {@link Constructor} in an {@link TokenCommand}.
     */
    private static final Map<Class<?>, List<TokenCommand>> CustomClassConstructors = new HashMap<>();

    /**
     * A {@link List} containing the registered {@link TokenSyntaxDefinition}.
     */
    private static final List<Method> RegisteredTokenSyntaxDefinitions = new ArrayList<>();

    static {
        registerTokenSyntax(TokenSyntaxDefinitions.class);
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
        if (isBuiltInType(customClass))
            throw new IllegalArgumentException(
                String.format("The class %s is a built in type.", customClass.getSimpleName()));

        if (CustomClassConstructors.containsKey(customClass))
            return;

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

        return parseCommand(constructorCommand, constructor.getParameterTypes());
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

            if (parameterType.isAssignableFrom(MessageReceivedEvent.class)) continue;

            String command = "#" + parameterType.getSimpleName();

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
        return new TokenCommand(instance, method, parseCommand(method));
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
        if (!method.isAnnotationPresent(Command.class))
            throw new IllegalCommandException(
                    String.format("Cannot parse the method %s as it isn't annotated with Command", method.getName()));

        Command command = method.getAnnotation(Command.class);
        Class<?>[] parameterTypes = method.getParameterTypes();
        int startParameterIndex = 0;

        if (0 < parameterTypes.length && parameterTypes[0].isAssignableFrom(MessageReceivedEvent.class))
            startParameterIndex = 1;

        String tokenCommand = command.command().isEmpty()
                ? generateCommand(parameterTypes, method.getParameterAnnotations())
                : command.command();

        return parseCommand(tokenCommand, parameterTypes, startParameterIndex, method.getParameterAnnotations());
    }

    /**
     * Generates a {@link List} of {@link CommandToken command tokens} from the specified {@link String command} and
     * the {@link Class parameter types}.
     *
     * @param command
     *      The {@link String} which describes the syntax of the command
     * @param parameterTypes
     *      The {@link Class parameter types} corresponding to the {@link Method command method}
     *
     * @return A {@link List} of {@link CommandToken command tokens} representing the command
     */
    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes)
    {
        return parseCommand(command, parameterTypes, 0, new Annotation[parameterTypes.length][0]);
    }

    /**
     * Generates a {@link List} of {@link CommandToken command tokens} from the specified information, which can be
     * automatically extracted from an {@link Method} by calling {@link TokenManager#parseCommand(Method)}.
     *
     * @param command
     *      The {@link String} which describes the syntax of the command
     * @param parameterTypes
     *      The {@link Class parameter types} corresponding to the {@link Method command method}
     * @param startParameterIndex
     *      The index of the parameter types to start generating the command from
     * @param parameterAnnotations
     *      The {@link Annotation annotations} which correspond to each parameter
     *
     * @return A {@link List} of {@link CommandToken command tokens} representing the command
     */
    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes,
                                                  int startParameterIndex, Annotation[][] parameterAnnotations)
    {
        List<String> tokens = splitCommandTokens(command);

        return parseCommandTokens(tokens, parameterTypes,
            parameterAnnotations, startParameterIndex);
    }

    public static List<String> splitInvocationTokens(@NotNull String command)
    {
        List<String> tokens = new ArrayList<>();

        if (command.isEmpty())
            return tokens;

        int startIndex = 0;
        int openBracketCount = 0;
        int currentIndex;

        for (currentIndex = 0; currentIndex < command.length(); currentIndex++) {
            char character = command.charAt(currentIndex);
            if ('[' == character) openBracketCount++;
            else if (']' == character) openBracketCount--;

            else if (0 == openBracketCount && ' ' == character) {
                tokens.add(command.substring(startIndex, currentIndex));
                startIndex = currentIndex + 1;
            }
        }

        //Add the final word, if there is one.
        if (startIndex != currentIndex)
            tokens.add(command.substring(startIndex, currentIndex));

        return tokens;
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
     * @param startingParameterTypeIndex
     *      The starting parameter index. E.g: The first index is usually a MessageReceivedEvent which doesn't get
     *      parsed into a command token
     *
     * @return The parsed {@link List<CommandToken>}
     */
    private static List<CommandToken> parseCommandTokens(List<String> tokens, Class<?>[] parameterTypes,
                                                         Annotation[][] parameterAnnotations, int startingParameterTypeIndex)
    {
        CommandTokenInfo commandTokenInfo = new CommandTokenInfo(tokens, parameterTypes, parameterAnnotations, startingParameterTypeIndex);
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
        String tokenIdentifier = token.substring(1);
        return getTypeAlias(type).equalsIgnoreCase(tokenIdentifier);
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
        return type.isAnnotationPresent(CustomParameter.class)
                ? type.getAnnotation(CustomParameter.class).identifier()
                : type.getSimpleName();
    }
}
